package MainWindow.Applications.Media;

public enum MediaType {
    VIDEO("video", "trình phát video"),
    AUDIO("audio", "trình phát âm thanh");

    private final String typeName;
    private final String playerDescription;

    MediaType(String typeName, String playerDescription) {
        this.typeName = typeName;
        this.playerDescription = playerDescription;
    }

    public String getTypeName() { return typeName; }
    public String getPlayerDescription() { return playerDescription; }
    public String getTypeNameCapitalized() { return typeName.substring(0, 1).toUpperCase() + typeName.substring(1); }
}
