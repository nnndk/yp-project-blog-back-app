package ru.yandex.practicum.unit.tools;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import ru.yandex.practicum.model.common.tools.ArrayDiff;
import ru.yandex.practicum.tools.ArrayDiffTools;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ArrayDiffToolsTest {

    private final ArrayDiffTools tools = new ArrayDiffTools();

    @Nested
    class GetItemsNotInLatter {

        @ParameterizedTest
        @NullSource
        @EmptySource
        void formerEmpty_returnsEmpty(String[] former) {
            assertThat(tools.getItemsNotInLatter(former, new String[]{"q"})).isEmpty();
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void latterEmpty_returnsAll(String[] latter) {
            assertThat(tools.getItemsNotInLatter(new String[]{"q", "w", "e"}, latter))
                    .containsExactlyInAnyOrder("q", "w", "e");
        }

        @ParameterizedTest
        @MethodSource("provideCommonElementInDifferentPositions")
        void oneCommon_returnsAllButOne(String[] former) {
            assertThat(tools.getItemsNotInLatter(former, new String[]{"q"}))
                    .containsExactlyInAnyOrder("w", "e");
        }

        private static Stream<Arguments> provideCommonElementInDifferentPositions() {
            return Stream.of(
                    Arguments.of((Object) new String[]{"q", "w", "e"}),
                    Arguments.of((Object) new String[]{"w", "q", "e"}),
                    Arguments.of((Object) new String[]{"e", "w", "q"})
            );
        }

        @Test
        void noCommon_returnsAll() {
            assertThat(tools.getItemsNotInLatter(new String[]{"q", "w", "e"}, new String[]{"a", "b"}))
                    .containsExactlyInAnyOrder("q", "w", "e");
        }

        @Test
        void allCommon_returnsEmpty() {
            assertThat(tools.getItemsNotInLatter(new String[]{"q", "w"}, new String[]{"q", "w", "e"}))
                    .isEmpty();
        }

        @Test
        void duplicatesInFormer_kept() {
            assertThat(tools.getItemsNotInLatter(new String[]{"q", "q", "w"}, new String[]{"w"}))
                    .containsExactlyInAnyOrder("q", "q");
        }

        @Test
        void duplicatesInLatter_ignored() {
            assertThat(tools.getItemsNotInLatter(new String[]{"q", "w"}, new String[]{"w", "w", "w"}))
                    .containsExactly("q");
        }

        @Test
        void caseSensitive() {
            assertThat(tools.getItemsNotInLatter(new String[]{"Q", "q"}, new String[]{"Q"}))
                    .containsExactly("q");
        }
    }

    @Nested
    class GetArrayDiff {

        @ParameterizedTest
        @NullSource
        void nullItemsAsIs_throws(String[] itemsAsIs) {
            assertThatThrownBy(() -> tools.getArrayDiff(itemsAsIs, new String[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemsAsIs");
        }

        @ParameterizedTest
        @NullSource
        void nullItemsToBe_throws(String[] itemsToBe) {
            assertThatThrownBy(() -> tools.getArrayDiff(new String[0], itemsToBe))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemsToBe");
        }

        @Test
        void bothEmpty_returnsEmpty() {
            ArrayDiff diff = tools.getArrayDiff(new String[0], new String[0]);
            assertThat(diff.itemsToAdd()).isEmpty();
            assertThat(diff.itemsToDelete()).isEmpty();
        }

        @Test
        void identicalArrays_returnsEmpty() {
            ArrayDiff diff = tools.getArrayDiff(
                    new String[]{"q", "w"},
                    new String[]{"q", "w"});
            assertThat(diff.itemsToAdd()).isEmpty();
            assertThat(diff.itemsToDelete()).isEmpty();
        }

        @Test
        void itemsAsIsEmpty_returnsAllInAdd() {
            ArrayDiff diff = tools.getArrayDiff(new String[0], new String[]{"q", "w"});
            assertThat(diff.itemsToAdd()).containsExactlyInAnyOrder("q", "w");
            assertThat(diff.itemsToDelete()).isEmpty();
        }

        @Test
        void itemsToBeEmpty_returnsAllInDelete() {
            ArrayDiff diff = tools.getArrayDiff(new String[]{"q", "w"}, new String[0]);
            assertThat(diff.itemsToAdd()).isEmpty();
            assertThat(diff.itemsToDelete()).containsExactlyInAnyOrder("q", "w");
        }

        @Test
        void partialOverlap_returnsCorrectDiff() {
            ArrayDiff diff = tools.getArrayDiff(
                    new String[]{"q", "w", "e"},
                    new String[]{"w", "e", "r", "t"});
            assertThat(diff.itemsToAdd()).containsExactlyInAnyOrder("r", "t");
            assertThat(diff.itemsToDelete()).containsExactly("q");
        }

        @Test
        void noOverlap_returnsAllInAddAndDelete() {
            ArrayDiff diff = tools.getArrayDiff(
                    new String[]{"q", "w"},
                    new String[]{"a", "b"});
            assertThat(diff.itemsToAdd()).containsExactlyInAnyOrder("a", "b");
            assertThat(diff.itemsToDelete()).containsExactlyInAnyOrder("q", "w");
        }

        @Test
        void duplicatesInAsIs_deduplicatedInDelete() {
            ArrayDiff diff = tools.getArrayDiff(
                    new String[]{"q", "q", "w"},
                    new String[]{"w"});
            assertThat(diff.itemsToDelete()).containsExactly("q");
            assertThat(diff.itemsToAdd()).isEmpty();
        }

        @Test
        void duplicatesInToBe_deduplicatedInAdd() {
            ArrayDiff diff = tools.getArrayDiff(
                    new String[0],
                    new String[]{"q", "q", "w"});
            assertThat(diff.itemsToAdd()).containsExactlyInAnyOrder("q", "w");
            assertThat(diff.itemsToDelete()).isEmpty();
        }

        @Test
        void caseSensitive_differsByCase() {
            ArrayDiff diff = tools.getArrayDiff(
                    new String[]{"Qwe"},
                    new String[]{"qwe"});
            assertThat(diff.itemsToAdd()).containsExactly("qwe");
            assertThat(diff.itemsToDelete()).containsExactly("Qwe");
        }
    }
}
