package com.github.awesomelemon;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

class MethodNamePrinter extends VoidVisitorAdapter<List<String>> {
    @Override
    public void visit(MethodDeclaration md, List<String> collector) {
        super.visit(md, collector);
//        System.out.println("Comment: " + md.getJavadocComment());
        collector.add(md.getNameAsString());
    }

    @Override
    public void visit(Parameter n, List<String> arg) {
        super.visit(n, arg);
        Type type = n.getType();
        arg.add(type.toString());
//        System.out.println(type);
    }
}