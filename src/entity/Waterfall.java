package entity;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/*
 * Waterfall — self-contained water cascade entity.
 *
 * It extends GameObject to integrate seamlessly into a level. The cascade is computed
 * based on the provided level solids, and it automatically builds a visual representation
 * of streams and horizontal flows.
 */
public final class Waterfall extends GameObject {

    //////// CONSTANTS ////////

    public static final double STREAM_WIDTH = 64.0;
    public static final double FLOW_HEIGHT = 20.0;
    public static final double FLOW_RISE = 14.0;

    private static final double OFFSCREEN_DROP = 120.0;
    private static final int MAX_DEPTH = 24;
    private static final int MAX_ZONES = 128;
    private static final double EPS = 0.5;
    
    private static final Color WATER_COLOR = Color.web("#38BDF8", 0.6);

    //////// FIELDS ////////

    private final double worldWidth;
    private final double worldHeight;
    private final List<SolidBlock> solids;
    
    private final List<Zone> streams = new ArrayList<>();
    private final List<Zone> flows = new ArrayList<>();
    
    private final Group visuals = new Group();

    //////// CONSTRUCTION ////////

    public Waterfall(double x, double y, double width, double worldWidth, double worldHeight, List<SolidBlock> solids) {
        super(x, y, width, worldHeight - y, Color.TRANSPARENT);
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.solids = solids;
        
        buildCascade();
        buildVisuals();
    }

    @Override
    public Node getNode() {
        return visuals;
    }

    //////// SIMULATION ////////

    private void buildCascade() {
        streams.clear();
        flows.clear();

        java.util.Map<SolidBlock, java.util.List<Double>> platformHits = new java.util.HashMap<>();
        java.util.Set<SolidBlock> processed = new java.util.HashSet<>();
        java.util.List<Drop> queue = new java.util.ArrayList<>();
        
        queue.add(new Drop(getX(), getY()));
        int head = 0;

        while (head < queue.size()) {
            Drop drop = queue.get(head++);
            SolidBlock surface = highestSurfaceBelow(drop.x, drop.y);

            double bottomY = surface != null ? surface.getY() : worldHeight + OFFSCREEN_DROP;
            streams.add(new Zone(drop.x - getWidth() / 2, drop.y, getWidth(), bottomY - drop.y, 0));

            if (surface != null) {
                platformHits.computeIfAbsent(surface, k -> new java.util.ArrayList<>()).add(drop.x);

                if (processed.add(surface)) {
                    double left = surface.getX();
                    double right = surface.getX() + surface.getWidth();
                    double top = surface.getY();

                    if (left > EPS) {
                        queue.add(new Drop(left, top));
                    }
                    if (right < worldWidth - EPS) {
                        queue.add(new Drop(right, top));
                    }
                }
            }
        }

        for (java.util.Map.Entry<SolidBlock, java.util.List<Double>> entry : platformHits.entrySet()) {
            SolidBlock surface = entry.getKey();
            java.util.List<Double> hits = entry.getValue();
            java.util.Collections.sort(hits);

            double left = surface.getX();
            double right = surface.getX() + surface.getWidth();
            double top = surface.getY();

            double firstHit = hits.get(0);
            if (firstHit > left) {
                flows.add(new Zone(left, top - FLOW_RISE, firstHit - left, FLOW_HEIGHT, -1));
            }

            for (int i = 0; i < hits.size() - 1; i++) {
                double x1 = hits.get(i);
                double x2 = hits.get(i + 1);
                if (x1 < x2) {
                    double mid = (x1 + x2) / 2.0;
                    flows.add(new Zone(x1, top - FLOW_RISE, mid - x1, FLOW_HEIGHT, 1));
                    flows.add(new Zone(mid, top - FLOW_RISE, x2 - mid, FLOW_HEIGHT, -1));
                }
            }

            double lastHit = hits.get(hits.size() - 1);
            if (right > lastHit) {
                flows.add(new Zone(lastHit, top - FLOW_RISE, right - lastHit, FLOW_HEIGHT, 1));
            }
        }
    }

    private SolidBlock highestSurfaceBelow(double centerX, double aboveY) {
        SolidBlock best = null;

        for (SolidBlock solid : solids) {
            double left = solid.getX();
            double right = solid.getX() + solid.getWidth();
            double top = solid.getY();

            if (centerX < left || centerX > right || top <= aboveY + EPS) {
                continue;
            }

            if (best == null || top < best.getY()) {
                best = solid;
            }
        }

        return best;
    }

    private void buildVisuals() {
        visuals.getChildren().clear();
        
        for (Zone stream : streams) {
            Rectangle rect = new Rectangle(stream.x, stream.y, stream.width, stream.height);
            rect.setFill(WATER_COLOR);
            visuals.getChildren().add(rect);
        }
        
        for (Zone flow : flows) {
            Rectangle rect = new Rectangle(flow.x, flow.y, flow.width, flow.height);
            rect.setFill(WATER_COLOR);
            visuals.getChildren().add(rect);
        }
    }

    //////// PLAYER RULES ////////

    public boolean isInStream(double px, double py, double pw, double ph) {
        for (Zone stream : streams) {
            if (overlaps(stream, px, py, pw, ph)) {
                return true;
            }
        }
        return false;
    }

    public double horizontalPush(double px, double py, double pw, double ph) {
        int sum = 0;
        for (Zone flow : flows) {
            if (overlaps(flow, px, py, pw, ph)) {
                sum += flow.direction;
            }
        }
        return Math.max(-1, Math.min(1, sum));
    }

    private boolean overlaps(Zone zone, double px, double py, double pw, double ph) {
        return px + pw > zone.x && px < zone.x + zone.width
            && py + ph > zone.y && py < zone.y + zone.height;
    }

    //////// ZONE ////////

    private static final class Drop {
        final double x;
        final double y;
        Drop(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class Zone {

        public final double x;
        public final double y;
        public final double width;
        public final double height;
        public final int direction;

        Zone(double x, double y, double width, double height, int direction) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.direction = direction;
        }
    }
}
