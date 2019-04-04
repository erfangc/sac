package com.erfangc.sac.core;

import java.util.List;

public class Node {
    private String name;
    private List<Node> children;

    public String getName() {
        return name;
    }

    public Node setName(String name) {
        this.name = name;
        return this;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node setChildren(List<Node> children) {
        this.children = children;
        return this;
    }
}
