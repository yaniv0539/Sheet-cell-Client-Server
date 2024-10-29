package engine.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {

    private final List<SingleChatEntry> chatDataList;

    private ChatManager() {
        chatDataList = new ArrayList<>();
    }

    public static ChatManager create() {
        return new ChatManager();
    }

    public synchronized void addChatString(String chatString, String username) {
        chatDataList.add(new SingleChatEntry(chatString, username));
    }

    public synchronized List<SingleChatEntry> getChatEntries(int fromIndex){
        if (fromIndex < 0 || fromIndex > chatDataList.size()) {
            fromIndex = 0;
        }
        return chatDataList.subList(fromIndex, chatDataList.size());
    }

    public int getVersion() {
        return chatDataList.size();
    }


}