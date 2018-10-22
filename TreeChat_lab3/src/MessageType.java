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
    }
}
