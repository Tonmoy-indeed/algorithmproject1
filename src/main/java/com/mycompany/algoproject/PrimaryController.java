
package com.mycompany.algoproject;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.*;

public class PrimaryController {

    @FXML private Pane graphPane;
    @FXML private StackPane node1, node2;
    @FXML private Circle circle1, circle2;
    @FXML private Button addButton1, removeButton1;

    private TextArea resultArea;
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private int nextId = 3;
    private Node selected = null;
    private boolean dragged = false;

    static class Node {
        int id;
        StackPane view;
        Circle circle;
        Node(int id, StackPane view, Circle circle) {
            this.id = id; this.view = view; this.circle = circle;
        }
    }

    static class Edge {
        Node u, v;
        double weight;
        Line line;
        Label tag;
        Edge(Node u, Node v, double weight, Line line, Label tag) {
            this.u = u; this.v = v; this.weight = weight; this.line = line; this.tag = tag;
        }
    }

    @FXML
    public void initialize() {
        for (javafx.scene.Node n : graphPane.getParent().getChildrenUnmodifiable()) {
            if (n instanceof TextArea) resultArea = (TextArea) n;
        }

        addButton1.setOnAction(e -> calculateMST());
        removeButton1.setOnAction(e -> clearAll());

        setupNode(new Node(1, node1, circle1));
        setupNode(new Node(2, node2, circle2));
    }

    @FXML
    void handleaddButton() {
        int id = nextId++;
        Circle c = new Circle(30, Color.TEAL);
        c.setStroke(Color.BLACK);
        c.setStrokeWidth(5);

        StackPane sp = new StackPane(c, new Label(String.valueOf(id)));
        sp.setLayoutX(260);
        sp.setLayoutY(200);

        graphPane.getChildren().add(sp);
        setupNode(new Node(id, sp, c));
    }

    @FXML
    void handleremoveButton() {
        if (nodes.isEmpty()) return;
        Node last = nodes.remove(nodes.size() - 1);
        graphPane.getChildren().remove(last.view);

        edges.removeIf(e -> {
            if (e.u == last || e.v == last) {
                graphPane.getChildren().removeAll(e.line, e.tag);
                return true;
            }
            return false;
        });
    }

    private void clearAll() {
        for (Node n : nodes) graphPane.getChildren().remove(n.view);
        for (Edge e : edges) graphPane.getChildren().removeAll(e.line, e.tag);
        nodes.clear();
        edges.clear();
        nextId = 1;
        selected = null;
        if (resultArea != null) resultArea.clear();
    }

    private void setupNode(Node n) {
        nodes.add(n);
        double[] d = new double[2];

        n.view.setOnMousePressed(e -> {
            dragged = false;
            d[0] = e.getSceneX() - n.view.getLayoutX();
            d[1] = e.getSceneY() - n.view.getLayoutY();
        });

        n.view.setOnMouseDragged(e -> {
            dragged = true;
            n.view.setLayoutX(e.getSceneX() - d[0]);
            n.view.setLayoutY(e.getSceneY() - d[1]);
        });

        n.view.setOnMouseClicked(e -> {
            if (dragged) return;
            if (selected == null) {
                selected = n;
                n.circle.setStroke(Color.ORANGE);
            } else if (selected == n) {
                n.circle.setStroke(Color.BLACK);
                selected = null;
            } else {
                Node first = selected;
                first.circle.setStroke(Color.BLACK);
                selected = null;
                connect(first, n);
            }
        });
    }

    private void connect(Node u, Node v) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setHeaderText("Weight between " + u.id + " and " + v.id);
        dialog.showAndWait().ifPresent(val -> {
            try {
                double w = Double.parseDouble(val.trim());
                Line line = new Line();
                line.setStrokeWidth(4);
                line.startXProperty().bind(u.view.layoutXProperty().add(30));
                line.startYProperty().bind(u.view.layoutYProperty().add(30));
                line.endXProperty().bind(v.view.layoutXProperty().add(30));
                line.endYProperty().bind(v.view.layoutYProperty().add(30));

                Label tag = new Label(val.trim());
                tag.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 2;");
                tag.layoutXProperty().bind(line.startXProperty().add(line.endXProperty()).divide(2));
                tag.layoutYProperty().bind(line.startYProperty().add(line.endYProperty()).divide(2));

                edges.add(new Edge(u, v, w, line, tag));
                graphPane.getChildren().add(1, line);
                graphPane.getChildren().add(tag);
            } catch (NumberFormatException ignored) {}
        });
    }

    private void calculateMST() {
        for (Edge e : edges) {
            e.line.setStroke(Color.LIGHTGRAY);
            e.line.setStrokeWidth(2);
        }

        List<Edge> sorted = new ArrayList<>(edges);
        sorted.sort(Comparator.comparingDouble(e -> e.weight));

        Map<Integer, Integer> parent = new HashMap<>();
        for (Node n : nodes) parent.put(n.id, n.id);

        double total = 0;
        for (Edge e : sorted) {
            int rootU = find(parent, e.u.id);
            int rootV = find(parent, e.v.id);
            if (rootU != rootV) {
                parent.put(rootU, rootV);
                total += e.weight;
                e.line.setStroke(Color.RED);
                e.line.setStrokeWidth(6);
            }
        }

        if (resultArea != null) resultArea.setText("Total MST Weight: " + total);
    }

    private int find(Map<Integer, Integer> parent, int id) {
        while (parent.get(id) != id) id = parent.get(id);
        return id;
    }
}