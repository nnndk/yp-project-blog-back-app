package ru.yandex.practicum.unit.tools;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.model.common.tools.SearchComponents;
import ru.yandex.practicum.tools.SearchTools;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SearchToolTest {

    private final SearchTools tools = new SearchTools();

    @Nested
    class DecomposeSearchTextBlankInput {

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        void blankOnly_returnsEmptyComponents(String input) {
            SearchComponents result = tools.decomposeSearchText(input);

            assertThat(result.tags()).isEmpty();
            assertThat(result.substring()).isEmpty();
        }

        @ParameterizedTest
        @NullSource
        void null_throws(String input) {
            assertThatThrownBy(() -> tools.decomposeSearchText(input))
                    .isInstanceOf(NullPointerException.class);
        }
    }


    @Nested
    class DecomposeSearchTextTagsOnly {

        @ParameterizedTest
        @MethodSource("tagsOnlyCases")
        void tagsOnly_extractedAsTags(String input, List<String> expectedTags) {
            SearchComponents result = tools.decomposeSearchText(input);

            assertThat(result.tags()).containsExactlyElementsOf(expectedTags);
            assertThat(result.substring()).isEmpty();
        }

        @Test
        void hashOnly_extractedAsEmptyTag() {
            SearchComponents result = tools.decomposeSearchText("#");

            // текущее поведение: "#" → tag = "", список непуст, hasTags() = true
            assertThat(result.tags()).containsExactly("");
            assertThat(result.substring()).isEmpty();
        }

        static Stream<Arguments> tagsOnlyCases() {
            return Stream.of(
                    Arguments.of("#tag_1", List.of("tag_1")),
                    Arguments.of("#tag_1 #tag_2", List.of("tag_1", "tag_2")),
                    Arguments.of("#tag_1   #tag_2", List.of("tag_1", "tag_2")),
                    Arguments.of("  #tag_1", List.of("tag_1")),
                    Arguments.of("#tag_1   ", List.of("tag_1"))
            );
        }
    }


    @Nested
    class DecomposeSearchTextWordsOnly {

        @ParameterizedTest
        @MethodSource("wordsOnlyCases")
        void wordsOnly_joinedIntoSubstring(String input, String expectedSubstring) {
            SearchComponents result = tools.decomposeSearchText(input);

            assertThat(result.tags()).isEmpty();
            assertThat(result.substring()).isEqualTo(expectedSubstring);
        }

        static Stream<Arguments> wordsOnlyCases() {
            return Stream.of(
                    Arguments.of("hello", "hello"),
                    Arguments.of("hello world", "hello world"),
                    Arguments.of("hello   world", "hello world"),
                    Arguments.of("  hello  ", "hello")
            );
        }
    }


    @Nested
    class DecomposeSearchTextMixed {

        @ParameterizedTest
        @MethodSource("mixedCases")
        void tagsAndWords_separated(String input, List<String> expectedTags, String expectedSubstring) {
            SearchComponents result = tools.decomposeSearchText(input);

            assertThat(result.tags()).containsExactlyElementsOf(expectedTags);
            assertThat(result.substring()).isEqualTo(expectedSubstring);
        }

        static Stream<Arguments> mixedCases() {
            return Stream.of(
                    Arguments.of("hello #tag_1", List.of("tag_1"), "hello"),
                    Arguments.of("#tag_1 hello", List.of("tag_1"), "hello"),
                    Arguments.of("hello #tag_1 world", List.of("tag_1"), "hello world"),
                    Arguments.of("#tag_1 hello #tag_2 world", List.of("tag_1", "tag_2"), "hello world"),
                    Arguments.of("hello   #tag_1   world", List.of("tag_1"), "hello world")
            );
        }
    }


    @Nested
    class SearchComponentsHasTags {

        @ParameterizedTest
        @NullSource
        void nullTags_returnsFalse(List<String> tags) {
            assertThat(new SearchComponents(tags, "hello").hasTags()).isFalse();
        }

        @Test
        void emptyTags_returnsFalse() {
            assertThat(new SearchComponents(List.of(), "hello").hasTags()).isFalse();
        }

        @Test
        void nonEmptyTags_returnsTrue() {
            assertThat(new SearchComponents(List.of("tag_1"), "hello").hasTags()).isTrue();
        }
    }


    @Nested
    class SearchComponentsHasSubstringToSearch {

        @ParameterizedTest
        @NullSource
        void nullSubstring_returnsFalse(String substring) {
            assertThat(new SearchComponents(List.of("tag_1"), substring).hasSubstringToSearch()).isFalse();
        }

        @Test
        void emptySubstring_returnsFalse() {
            assertThat(new SearchComponents(List.of("tag_1"), "").hasSubstringToSearch()).isFalse();
        }

        @Test
        void nonEmptySubstring_returnsTrue() {
            assertThat(new SearchComponents(List.of(), "hello").hasSubstringToSearch()).isTrue();
        }
    }
}