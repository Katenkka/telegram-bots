package net.chmilevfa.telegram.bots.currency.dao.file;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.chmilevfa.telegram.bots.currency.Currencies;
import net.chmilevfa.telegram.bots.currency.state.MessageState;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class that represents state of currency bot.
 *
 * @author chmilevfa
 * @since 09.07.18
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class InMemoryData {

    private Map<Integer, String> languages = new HashMap<>();

    @JsonSerialize(keyUsing = DialogIdSerializer.class)
    @JsonDeserialize(keyUsing = DialogIdDeserializer.class)
    private Map<DialogId, MessageState> states = new HashMap<>();

    /** Stores the first currency from the pair and will be used in the next step */
    private Map<Long, Currencies> firstUserCurrency = new HashMap<>();

    void saveLanguage(Integer userId, String language) {
        languages.put(userId, language);
    }

    String getLanguage(Integer userId) {
        return languages.get(userId);
    }

    void saveMessageState(Integer userId, Long chatId, MessageState state) {
        DialogId dialogId = new DialogId(userId, chatId);
        states.put(dialogId, state);
    }

    MessageState getMessageState(Integer userId, Long chatId) {
        DialogId dialogId = new DialogId(userId, chatId);
        return states.getOrDefault(dialogId, MessageState.MAIN_MENU);
    }

    void saveFirstUserCurrency(Long chatId, Currencies currency) {
        firstUserCurrency.put(chatId, currency);
    }

    Currencies getFirstUserCurrency(Long chatId) {
        return firstUserCurrency.remove(chatId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryData that = (InMemoryData) o;
        return Objects.equals(languages, that.languages) &&
                Objects.equals(states, that.states) &&
                Objects.equals(firstUserCurrency, that.firstUserCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languages, states, firstUserCurrency);
    }

    /**
     * ID based on user and chat identifiers.
     */
    private static class DialogId {

        private final static String SERIALIZATION_SEPARATOR = "_";

        private Integer userId;
        private Long chatId;

        DialogId(Integer userId, Long chatId) {
            this.userId = userId;
            this.chatId = chatId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DialogId dialogId = (DialogId) o;
            return Objects.equals(userId, dialogId.userId) &&
                    Objects.equals(chatId, dialogId.chatId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, chatId);
        }
    }

    /**
     * Custom serializer for {@link DialogId}.
     */
    private static class DialogIdSerializer extends JsonSerializer<DialogId> {
        @Override
        public void serialize(DialogId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeFieldName(value.userId + DialogId.SERIALIZATION_SEPARATOR + value.chatId);
        }
    }

    /**
     * Custom deserializer for {@link DialogId}.
     */
    private static class DialogIdDeserializer extends KeyDeserializer {
        @Override
        public DialogId deserializeKey(String key, DeserializationContext ctxt) {
            String[] pairs = key.split(DialogId.SERIALIZATION_SEPARATOR);

            Integer userId = Integer.parseInt(pairs[0].trim());
            Long chatId = Long.parseLong(pairs[1].trim());

            return new DialogId(userId, chatId);
        }
    }
}
