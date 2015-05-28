package mw.path;

import mw.facebookclient.MWMyFacebookService;
import mw.facebookclient.MWUnknownIDException_Exception;
import net.java.dev.jaxb.array.StringArray;
import net.java.dev.jaxb.array.StringArrayArray;

import java.util.*;


public class MWDijkstra {

    /*
     * Calculates the shortest path between startID and endID.
     */
    public static String[] getShortestPath(MWMyFacebookService facebook, Collection<String> graph, String startID, String endID) {
        // Dijkstra's algorithm
        System.out.println("DIJKSTRA: " + graph.size() + " nodes, startID = " + startID + ", endID = " + endID);
        long startTime = System.currentTimeMillis();

        // Initialization
        List<MWDijkstraNode> remainingNodes = new LinkedList<MWDijkstraNode>();
        for (String id : graph) {
            if (id.equals(startID)) {
                continue;
            }
            remainingNodes.add(new MWDijkstraNode(id));
        }

        MWDijkstraNode dNodeA = new MWDijkstraNode(startID);
        dNodeA.setDistance(0);
        remainingNodes.add(0, dNodeA);


        // Retrieve friends
        // Create id net.java.dev.jaxb.array
        StringArray ids = new StringArray();
        for (MWDijkstraNode node : remainingNodes) {
            ids.getItem().add(node.getID());
        }

        // Store batch response
        try {
            StringArrayArray friendIDsArrayArray = facebook.getFriendsBatch(ids);
            Iterator<MWDijkstraNode> nodeIterator = remainingNodes.iterator();
            for (StringArray friendIDs : friendIDsArrayArray.getItem()) {
                nodeIterator.next().setFriends(friendIDs.getItem().toArray(new String[0]));
            }
        } catch (MWUnknownIDException_Exception uie) {
            System.err.println("FACEBOOK ERROR: " + uie);
            return null;
        }


        // Create predecessor information
        MWDijkstraNode dNodeB = null;
        while (!remainingNodes.isEmpty()) {
            if ((remainingNodes.size() % 500) == 0) {
                System.out.println(String.format("DIJKSTRA: %4d nodes remaining to be processed", remainingNodes.size()));
            }

            Collections.sort(remainingNodes);

            MWDijkstraNode dNode = remainingNodes.remove(0);
            if (dNode.getID().equals(endID)) {
                dNodeB = dNode;
            }

            for (String friend : dNode.getFriends()) {
                MWDijkstraNode dFriend = null;
                for (MWDijkstraNode dn : remainingNodes) {
                    if (dn.getID().equals(friend)) {
                        dFriend = dn;
                        break;
                    }
                }
                if (dFriend == null) {
                    continue;
                }

                int alternative = dNode.getDistance() + 1;
                if (alternative < dFriend.getDistance()) {
                    dFriend.setDistance(alternative);
                    dFriend.setPredecessor(dNode);
                }
            }
        }

        // Extract shortest path
        List<String> shortestPath = new LinkedList<String>();
        shortestPath.add(endID);

        MWDijkstraNode dn = dNodeB;
        while (dn.getPredecessor() != null) {
            dn = dn.getPredecessor();
            shortestPath.add(0, dn.getID());
        }


        System.out.println("DIJKSTRA: completed in " + (System.currentTimeMillis() - startTime) + "ms");

        return shortestPath.toArray(new String[shortestPath.size()]);
    }


    /*
     * Helper class for calculating the shortest path between two nodes.
     */
    private static class MWDijkstraNode implements Comparable<MWDijkstraNode> {

        private final String id;
        private int distance;
        private String[] friends;
        private MWDijkstraNode predecessor;


        public MWDijkstraNode(String id) {
            this.id = id;
            distance = Integer.MAX_VALUE;
        }


        public String getID() {
            return id;
        }


        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }


        public String[] getFriends() {
            return friends;
        }

        public void setFriends(String[] friends) {
            this.friends = friends;
        }


        public MWDijkstraNode getPredecessor() {
            return predecessor;
        }

        public void setPredecessor(MWDijkstraNode predecessor) {
            this.predecessor = predecessor;
        }


        public int compareTo(MWDijkstraNode o) {
            return (distance - o.distance);
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MWDijkstraNode) {
                return id.equals(((MWDijkstraNode) obj).id);
            }
            return false;
        }

        @Override
        public String toString() {
            return "{" + id + "#" + distance + "}";
        }

    }

}
