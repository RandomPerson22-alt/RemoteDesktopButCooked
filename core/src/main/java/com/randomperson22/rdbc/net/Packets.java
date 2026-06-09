package com.randomperson22.rdbc.net;

public class Packets {

    public static abstract class Packet {
        public String type;
    }

    public static class MetaPacket extends Packet {

        public int width;
        public int height;

        public MetaPacket() {
            type = "meta";
        }

        public MetaPacket(int width, int height) {
            this();
            this.width = width;
            this.height = height;
        }
    }

    public static class PingPacket extends Packet {

        public long timestamp;

        public PingPacket() {
            type = "ping";
        }

        public PingPacket(long timestamp) {
            this();
            this.timestamp = timestamp;
        }
    }
}