package com.github.awesomelemon;

public class Method {
    private String javadocComment;
    private String callSequence;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public String getJavadocComment() {
        return javadocComment;
    }

    public void setJavadocComment(String javadocComment) {
        this.javadocComment = javadocComment;
    }

    public String getCallSequence() {
        return callSequence;
    }

    public void setCallSequence(String callSequence) {
        this.callSequence = callSequence;
    }

    public Method(String javadocComment, String callSequence, String name) {
        this.javadocComment = javadocComment;
        this.callSequence = callSequence;
        this.name = name;
    }
}
