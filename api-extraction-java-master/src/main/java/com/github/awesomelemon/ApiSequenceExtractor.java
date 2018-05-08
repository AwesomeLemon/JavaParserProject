package com.github.awesomelemon;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.UnsolvedSymbolException;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static com.github.awesomelemon.Util.getShortTypeName;

public class ApiSequenceExtractor extends VoidVisitorAdapter<List<ApiCall>> {
    private TypeSolver typeSolver;
    private String lastReturnType;

    ApiSequenceExtractor(TypeSolver typeSolver) {
        super();
        this.typeSolver = typeSolver;
    }

    private <N extends Node, T extends Type> void updateLastReturnType(NodeWithType<N, T> nodeWithType) {
        lastReturnType = nodeWithType.getType().toString();
    }

    private void updateLastReturnType(String typeString) {
        lastReturnType = typeString;
    }

    @Override
    public void visit(ObjectCreationExpr oc, List<ApiCall> calls) {
        NodeList<Expression> arguments = oc.getArguments();
        if (arguments.size() > 0) {
            for (Expression argument : arguments) {
                argument.accept(this, calls);
            }
        }
        String typeName = solveType(oc);
        if (typeName == null) {
            lastReturnType = null;
        }
        else {
            calls.add(ApiCall.OfConstructor(typeName));
            updateLastReturnType(oc);
        }
    }

    private String solveType(Expression expression) {
        try {
            return JavaParserFacade.get(typeSolver).getType(expression).describe();
        }
        catch (UnsolvedSymbolException e) {
//            System.out.println(1);
            e.printStackTrace();
            return getName(e);
        }
        catch (UnsupportedOperationException e) {
//            System.out.println(2);
            e.printStackTrace();
            return null;
        }
        catch (RuntimeException e ) {
            System.out.println(3);
            return null;
        }
    }

    private String getName(UnsolvedSymbolException e) {
        try {
            Field f = e.getClass().getDeclaredField("name"); //NoSuchFieldException
            f.setAccessible(true);
            return (String) f.get(e);
        } catch (IllegalAccessException | NoSuchFieldException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    public void visit(TryStmt tryStmt, List<ApiCall> calls) {
        Stack<ApiCall> resourcesToClose = new Stack<>();
        tryStmt.getResources().forEach(resource -> {
            resource.ifVariableDeclarationExpr(variableDeclarationExpr -> {
                variableDeclarationExpr.getVariables().forEach(v -> {
                    resourcesToClose.push(ApiCall.OfMethodInvocation(v, "close"));
                });
            });
            resource.accept(this, calls);
        });
        tryStmt.getTryBlock().accept(this, calls);
        while (!resourcesToClose.empty()) {
            calls.add(resourcesToClose.pop());
        }
    }

    @Override
    public void visit(StringLiteralExpr n, List<ApiCall> calls) {
        updateLastReturnType("java.lang.String");
    }

    static final HashSet<String> streamApiFuns = new HashSet<>(List.of("anyMatch", "collect", "count", "distinct",
            "filter", "findAny", "findFirst", "flatMap", "flatMapToDouble", "flatMapToInt",
            "flatMapToLong", "forEach", "forEachOrdered", "limit", "map", "mapToDouble", "mapToInt",
            "mapToLong", "max", "min", "noneMatch", "peek", "reduce", "skip", "sorted"));

    @Override
    public void visit(MethodCallExpr methodCallExpr, List<ApiCall> calls) {
        Optional<Expression> scope = methodCallExpr.getScope();
        if (scope.isPresent()) {
            Expression scopeExpr = scope.get();
            scopeExpr.accept(this, calls);//suppose that the type of this was written to lastReturnType
            String scopeType = lastReturnType;
            if (lastReturnType == null) return;

            //stream API calls should be processed differently
            //in normal calls the execution order is scope-arguments-function
            //however, if the function is a stream function, the execution goes scope-function-arguments
            //arguably, for me Stream functions are irrelevant, 'cause they are separate from the functionality-related API calls
            if (streamApiFuns.contains(methodCallExpr.getNameAsString())) {
                calls.add(ApiCall.OfMethodInvocation(scopeType, methodCallExpr.getNameAsString()));
                methodCallExpr.getArguments().forEach(arg -> arg.accept(this, calls));
            }
            else {
                methodCallExpr.getArguments().forEach(arg -> arg.accept(this, calls));
                calls.add(ApiCall.OfMethodInvocation(scopeType, methodCallExpr.getNameAsString()));
            }

            updateLastReturnTypeOfMethod(methodCallExpr);
        } else {
            //we're calling a local function. Recording its name is probably useless, so let's visit it instead
            //on the other hand it seems complicated, so I'll postpone working on it for now
            //arguments can be easilly processed right now
            methodCallExpr.getArguments().forEach(arg -> arg.accept(this, calls));
        }
    }

    private void updateLastReturnTypeOfMethod(MethodCallExpr methodCallExpr) {
        MethodUsage methodUsage = null;
        try {
//            System.out.println("updateLastReturnType: " + methodCallExpr.getName());
//            ImmutableList<String> exceptionMethodNames = ImmutableList.of("isLoggable", "d", "load"); // For "Cropiwa" repository
            ImmutableList<String> exceptionMethodNames = ImmutableList.of("writeObject", "callable", "toMicros", "hashCode", "divide", "log2", "doubleValue"); // For "Guava" repository
            if (exceptionMethodNames.contains(methodCallExpr.getName().asString())) {
                System.out.println("EXCEPTION");
                lastReturnType = null;
                return;
            }
            methodUsage = JavaParserFacade.get(typeSolver).solveMethodAsUsage(methodCallExpr);
        }
        catch (UnsolvedSymbolException e) {
//            System.out.println(4);
            lastReturnType = null;
            e.printStackTrace();
        }
        catch (RuntimeException e) {//this is neccessary, 'cause JavaParser can throw these when it fails
//            System.out.println(5);
            lastReturnType = null;
            e.printStackTrace();
        }
        if (methodUsage != null) {
//            System.out.println(6);
            String shortType = getShortTypeName(methodUsage);
            updateLastReturnType(shortType);
        }
    }

    @Override
    public void visit(CastExpr n, List<ApiCall> calls) {
        n.getExpression().accept(this, calls);
        updateLastReturnType(n);
    }

    @Override
    public void visit(DoubleLiteralExpr n, List<ApiCall> calls) {
        updateLastReturnType("java.lang.Double");
    }

    @Override
    public void visit(IntegerLiteralExpr n, List<ApiCall> calls) {
        updateLastReturnType("java.lang.Integer");
    }

    @Override
    public void visit(IfStmt n, List<ApiCall> calls) {
        n.getCondition().accept(this, calls);
        n.getThenStmt().accept(this, calls);
        n.getElseStmt().ifPresent((l) -> {
            l.accept(this, calls);
        });
    }

    @Override
    public void visit(NameExpr n, List<ApiCall> calls) {
        if (Character.isUpperCase(n.getNameAsString().charAt(0))) {

            //probably this is type, not a variable name.
            updateLastReturnType(n.getNameAsString());
            return;
        }
//        System.out.println(n.getName());
        String type = solveType(n);
        if (type == null) {
            lastReturnType = null;
        }
        else {
            String shortTypeName = getShortTypeName(type);
//        System.out.println(shortTypeName);
            updateLastReturnType(shortTypeName);
        }
    }

    @Override
    public void visit(Name n, List<ApiCall> calls) {
        updateLastReturnType(getShortTypeName(n.asString()));
    }

    //useless, for I do not go inside constructors, and 'super' can be only there
    //also, unfinished
//    @Override
//    public void visit(SuperExpr n, List<ApiCall> calls) {
//        n.getClassExpr().ifPresent((l) -> {
//            l.accept(this, calls);
//        });
//        ResolvedType type = JavaParserFacade.get(typeSolver).getType(n);
//    }

    @Override
    public void visit(ThisExpr n, List<ApiCall> calls) {
        updateLastReturnType(solveType(n));
    }

    //now useless, 'cause I get variable type from the left of the expression, and this allows me to get it from the right.
    //but I don't do it.
    @Override
    public void visit(VariableDeclarationExpr n, List<ApiCall> calls) {
        n.getVariables().forEach((p) -> {
            p.accept(this, calls);
        });
    }

    @Override
    public void visit(WhileStmt n, List<ApiCall> calls) {
        n.getCondition().accept(this, calls);
        n.getBody().accept(this, calls);
    }

    @Override
    public void visit(MethodReferenceExpr n, List<ApiCall> calls) {
        n.getScope().accept(this, calls);
        n.getTypeArguments().ifPresent((l) -> {
            l.forEach((v) -> {
                v.accept(this, calls);
            });
        });
//        System.out.println(n.getScope().toString());
        String type = solveType(n.getScope());
        if (type == null) {
            lastReturnType = null;
        }
        else {
            calls.add(ApiCall.OfMethodInvocation(type, n.getIdentifier()));
            updateLastReturnType(type);
        }
//        ApiCall.OfMethodInvocation()
    }

    //I hoped that processing body last would solve StackOverflow, since parameters would already
    //have been solved. Unfortunately, no.
    @Override
    public void visit(final MethodDeclaration n, final List<ApiCall> calls) {
        n.getType().accept(this, calls);
        n.getName().accept(this, calls);
        n.getParameters().forEach(p -> p.accept(this, calls));
        n.getReceiverParameter().ifPresent(l -> l.accept(this, calls));
        n.getThrownExceptions().forEach(p -> p.accept(this, calls));
        n.getTypeParameters().forEach(p -> p.accept(this, calls));
        n.getAnnotations().forEach(p -> p.accept(this, calls));
        n.getComment().ifPresent(l -> l.accept(this, calls));
        n.getBody().ifPresent(l -> l.accept(this, calls));
    }
    //    @Override
//    public void visit(ObjectCreationExpr n, List<ApiCall> calls) {
//        if (n.getAnonymousClassBody().isPresent()) return;
//        n.getScope().ifPresent((l) -> {
//            //I don't really know why anyone would write "new a().new b()", but whatever.
//            l.accept(this, calls);
//        });
//        n.getType().accept(this, calls);
//    }

    @Override
    public void visit(final FieldAccessExpr n, final List<ApiCall> arg) {
//        System.out.println(n.getName());
        Expression scope = n.getScope();
//        if (scope instanceof NameExpr) {
//            NameExpr scopeName = (NameExpr) scope;
//            if (scopeName.getName().asString().equals(n.getName().asString())) {
//                return;
//            }
//        }
//        if (scope instanceof FieldAccessExpr) {
//            String scopeField = ((FieldAccessExpr) scope).getScope().toString();
//            if (scopeField.equals(n.getName().asString())) {
//                return;
//            }
//        }
        n.getName().accept(this, arg);
        scope.accept(this, arg);
        n.getTypeArguments().ifPresent(l -> l.forEach(v -> v.accept(this, arg)));
        n.getComment().ifPresent(l -> l.accept(this, arg));
    }
}
