package ru.yandex.practicum.tools;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.common.tools.ArrayDiff;

import java.util.*;

@Component
public class ArrayDiffTools {

    public List<String> getItemsNotInLatter(String[] former, String[] latter) {
        if (former == null || former.length == 0) {
            return List.of();
        }
        if (latter == null || latter.length == 0) {
            return Arrays.stream(former).toList();
        }

        Set<String> latterSet = new HashSet<>(Arrays.asList(latter));

        return Arrays.stream(former)
                .filter(s -> !latterSet.contains(s))
                .toList();
    }

    public ArrayDiff getArrayDiff(String[] itemsAsIs, String[] itemsToBe) {
        if (itemsAsIs == null) {
            throw new IllegalArgumentException("`itemsAsIs` cannot be null");
        }
        if (itemsToBe == null) {
            throw new IllegalArgumentException("`itemsToBe` cannot be null");
        }

        Set<String> current = new HashSet<>(Arrays.asList(itemsAsIs));
        Set<String> target  = new HashSet<>(Arrays.asList(itemsToBe));

        List<String> toAdd = new ArrayList<>();
        for (String tag : target) {
            if (!current.contains(tag)) {
                toAdd.add(tag);
            }
        }

        List<String> toDelete = new ArrayList<>();
        for (String tag : current) {
            if (!target.contains(tag)) {
                toDelete.add(tag);
            }
        }

        return new ArrayDiff(toAdd, toDelete);
    }
}
