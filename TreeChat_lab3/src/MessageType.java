public enum MessageType {
    TEXT_MESSAGE {
        @Override
        public String toString() {
            return "message";
        }
    },
    CONFIRMATION {
        @Override
        public String toString() {
            return "confirmation";
        }
    },
    I_AM_YOUR_SON {
        @Override
        public String toString() {
            return "child";
        }
    }
}
