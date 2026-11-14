package ru.yandex.practicum.tools;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.common.tools.SearchComponents;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchTools {

    public SearchComponents decomposeSearchText(String searchText) {
        String[] words = searchText.split(" ");
        List<String> tags = new ArrayList<>();
        List<String> partsOfSubstring = new ArrayList<>();

        for (String word : words) {
            if (word == null || word.isBlank()) {
                continue;
            }

            if (isTag(word)) {
                String tag = word.substring(1);
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            } else {
                partsOfSubstring.add(word);
            }
        }


        return new SearchComponents(
                tags,
                String.join(" ", partsOfSubstring.toArray(String[]::new))
        );
    }

    private boolean isTag(String word) {
        return word.startsWith("#");
    }
}
