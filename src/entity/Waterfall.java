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

    private final List<double[]> sources;
    private final double worldWidth;
    private final double worldHeight;
    private final List<SolidBlock> solids;

    private final List<Zone> streams = new ArrayList<>();
    private final List<Zone> flows = new ArrayList<>();

    private final Group visuals = new Group();

    //////// CONSTRUCTION ////////

    public Waterfall(double x, double y, double width, double worldWidth, double worldHeight, List<SolidBlock> solids) {
        this(List.of(new double[] { x, y }), width, worldWidth, worldHeight, solids);
    }

    /*
     * Multi-source constructor — seeds every source into the same cascade so that
     * sources landing on a shared platform converge and meet at their midpoint
     * instead of each spreading past the other and canceling out over the whole span.
     */
    public Waterfall(List<double[]> sources, double width, double worldWidth, double worldHeight,
            List<SolidBlock> solids) {
        super(sources.get(0)[0], sources.get(0)[1], width, worldHeight - sources.get(0)[1], Color.TRANSPARENT);
        this.sources = sources;
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

        java.util.List<Drop> queue = new java.util.ArrayList<>();
        java.util.Set<String> processedDrops = new java.util.HashSet<>();
        
        java.util.function.Consumer<Drop> enqueue = (d) -> {
            String key = String.format("%.1f,%.1f", d.x, d.y);
            if (processedDrops.add(key)) {
                queue.add(d);
            }
        };

        for (double[] source : sources) {
            enqueue.accept(new Drop(source[0], source[1], null));
        }
        int head = 0;
        
        java.util.Map<SolidBlock, java.util.List<Double>> platformHits = new java.util.HashMap<>();

        while (head < queue.size()) {
            Drop drop = queue.get(head++);
            
            SolidBlock surface = null;
            double streamLeft = drop.x - STREAM_WIDTH / 2.0;
            double streamRight = drop.x + STREAM_WIDTH / 2.0;

            for (SolidBlock solid : solids) {
                if (solid == drop.source) continue;
                
                double left = solid.getX();
                double right = solid.getX() + solid.getWidth();
                double top = solid.getY();

                if (streamRight < left + EPS || streamLeft > right - EPS || top < drop.y - EPS) {
                    continue;
                }

                if (surface == null || top < surface.getY()) {
                    surface = solid;
                }
            }

            double bottomY = surface != null ? surface.getY() : worldHeight + OFFSCREEN_DROP;
            double streamHeight = Math.max(0, bottomY - drop.y);
            streams.add(new Zone(drop.x - getWidth() / 2, drop.y, getWidth(), streamHeight, 0));

            if (surface != null) {
                double top = surface.getY();
                double startX = Math.max(surface.getX(), Math.min(drop.x, surface.getX() + surface.getWidth()));
                
                platformHits.computeIfAbsent(surface, k -> new java.util.ArrayList<>()).add(startX);
                
                // Trace left for drop enqueue
                double flowMinX = surface.getX();
                boolean blockedLeft = false;
                for (SolidBlock solid : solids) {
                    if (solid == surface) continue;
                    double sLeft = solid.getX();
                    double sRight = solid.getX() + solid.getWidth();
                    double sTop = solid.getY();
                    double sBottom = solid.getY() + solid.getHeight();
                    
                    double flowTop = top - FLOW_RISE;
                    double flowBottom = flowTop + FLOW_HEIGHT;
                    
                    if (flowBottom > sTop + EPS && flowTop < sBottom - EPS) {
                        if (sRight <= startX + EPS && sRight >= flowMinX - EPS) {
                            flowMinX = Math.max(flowMinX, sRight);
                            if (Math.abs(flowMinX - sRight) < EPS) {
                                blockedLeft = true;
                            }
                        }
                    }
                }
                
                if (!blockedLeft && Math.abs(flowMinX - surface.getX()) < EPS && flowMinX > EPS) {
                    enqueue.accept(new Drop(flowMinX, top, surface));
                }

                // Trace right for drop enqueue
                double flowMaxX = surface.getX() + surface.getWidth();
                boolean blockedRight = false;
                for (SolidBlock solid : solids) {
                    if (solid == surface) continue;
                    double sLeft = solid.getX();
                    double sRight = solid.getX() + solid.getWidth();
                    double sTop = solid.getY();
                    double sBottom = solid.getY() + solid.getHeight();
                    
                    double flowTop = top - FLOW_RISE;
                    double flowBottom = flowTop + FLOW_HEIGHT;
                    
                    if (flowBottom > sTop + EPS && flowTop < sBottom - EPS) {
                        if (sLeft >= startX - EPS && sLeft <= flowMaxX + EPS) {
                            flowMaxX = Math.min(flowMaxX, sLeft);
                            if (Math.abs(flowMaxX - sLeft) < EPS) {
                                blockedRight = true;
                            }
                        }
                    }
                }
                
                if (!blockedRight && Math.abs(flowMaxX - (surface.getX() + surface.getWidth())) < EPS && flowMaxX < worldWidth - EPS) {
                    enqueue.accept(new Drop(flowMaxX, top, surface));
                }
            }
        }
        
        // Build horizontal flows
        for (java.util.Map.Entry<SolidBlock, java.util.List<Double>> entry : platformHits.entrySet()) {
            SolidBlock surface = entry.getKey();
            java.util.List<Double> hits = entry.getValue();
            java.util.Collections.sort(hits);
            
            java.util.List<Double> uniqueHits = new java.util.ArrayList<>();
            for (double h : hits) {
                if (uniqueHits.isEmpty() || Math.abs(uniqueHits.get(uniqueHits.size() - 1) - h) > EPS) {
                    uniqueHits.add(h);
                }
            }
            
            double top = surface.getY();
            
            double firstHit = uniqueHits.get(0);
            double flowMinX = traceLeftBound(surface, firstHit);
            if (firstHit - flowMinX > EPS) {
                flows.add(new Zone(flowMinX, top - FLOW_RISE, firstHit - flowMinX, FLOW_HEIGHT, -1));
            }
            
            for (int i = 0; i < uniqueHits.size() - 1; i++) {
                double x1 = uniqueHits.get(i);
                double x2 = uniqueHits.get(i + 1);
                
                double maxR = traceRightBound(surface, x1);
                double minL = traceLeftBound(surface, x2);
                
                if (maxR < minL - EPS) {
                    if (maxR - x1 > EPS) {
                        flows.add(new Zone(x1, top - FLOW_RISE, maxR - x1, FLOW_HEIGHT, 1));
                    }
                    if (x2 - minL > EPS) {
                        flows.add(new Zone(minL, top - FLOW_RISE, x2 - minL, FLOW_HEIGHT, -1));
                    }
                } else {
                    double mid = (x1 + x2) / 2.0;
                    flows.add(new Zone(x1, top - FLOW_RISE, mid - x1, FLOW_HEIGHT, 1));
                    flows.add(new Zone(mid, top - FLOW_RISE, x2 - mid, FLOW_HEIGHT, -1));
                }
            }
            
            double lastHit = uniqueHits.get(uniqueHits.size() - 1);
            double flowMaxX = traceRightBound(surface, lastHit);
            if (flowMaxX - lastHit > EPS) {
                flows.add(new Zone(lastHit, top - FLOW_RISE, flowMaxX - lastHit, FLOW_HEIGHT, 1));
            }
        }
    }

    private double traceLeftBound(SolidBlock surface, double startX) {
        double flowMinX = surface.getX();
        for (SolidBlock solid : solids) {
            if (solid == surface) continue;
            double sRight = solid.getX() + solid.getWidth();
            double sTop = solid.getY();
            double sBottom = solid.getY() + solid.getHeight();
            double flowTop = surface.getY() - FLOW_RISE;
            double flowBottom = flowTop + FLOW_HEIGHT;
            if (flowBottom > sTop + EPS && flowTop < sBottom - EPS) {
                if (sRight <= startX + EPS && sRight >= flowMinX - EPS) {
                    flowMinX = Math.max(flowMinX, sRight);
                }
            }
        }
        return flowMinX;
    }

    private double traceRightBound(SolidBlock surface, double startX) {
        double flowMaxX = surface.getX() + surface.getWidth();
        for (SolidBlock solid : solids) {
            if (solid == surface) continue;
            double sLeft = solid.getX();
            double sTop = solid.getY();
            double sBottom = solid.getY() + solid.getHeight();
            double flowTop = surface.getY() - FLOW_RISE;
            double flowBottom = flowTop + FLOW_HEIGHT;
            if (flowBottom > sTop + EPS && flowTop < sBottom - EPS) {
                if (sLeft >= startX - EPS && sLeft <= flowMaxX + EPS) {
                    flowMaxX = Math.min(flowMaxX, sLeft);
                }
            }
        }
        return flowMaxX;
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
        final SolidBlock source;

        Drop(double x, double y, SolidBlock source) {
            this.x = x;
            this.y = y;
            this.source = source;
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
