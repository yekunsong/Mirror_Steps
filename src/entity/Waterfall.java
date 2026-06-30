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
        fall(getX(), getY(), 0);
    }

    private void fall(double centerX, double topY, int depth) {
        if (depth > MAX_DEPTH || streams.size() + flows.size() > MAX_ZONES) {
            return;
        }

        SolidBlock surface = highestSurfaceBelow(centerX, topY);
        double bottomY = surface != null ? surface.getY() : worldHeight + OFFSCREEN_DROP;
        streams.add(new Zone(centerX - getWidth() / 2, topY, getWidth(), bottomY - topY, 0));

        if (surface == null) {
            return;
        }

        double left = surface.getX();
        double right = surface.getX() + surface.getWidth();
        double top = surface.getY();

        if (centerX > left) {
            flows.add(new Zone(left, top - FLOW_RISE, centerX - left, FLOW_HEIGHT, -1));
        }

        if (right > centerX) {
            flows.add(new Zone(centerX, top - FLOW_RISE, right - centerX, FLOW_HEIGHT, 1));
        }

        if (left > EPS) {
            fall(left, top, depth + 1);
        }

        if (right < worldWidth - EPS) {
            fall(right, top, depth + 1);
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
