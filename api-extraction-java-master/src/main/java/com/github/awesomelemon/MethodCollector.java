package com.github.awesomelemon;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Optional;

public class MethodCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {
    @Override
    public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
        super.visit(md, collector);
        Optional<JavadocComment> javadocComment = md.getJavadocComment();
        if (javadocComment.isPresent()) {
            collector.add(md);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<MethodDeclaration> collector) {
        if (n.isInterface()) return;
        super.visit(n, collector);
    }
}