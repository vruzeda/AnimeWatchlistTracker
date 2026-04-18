package com.vuzeda.animewatchlist.tracker.module.usecase

import com.google.common.truth.Truth.assertThat
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import org.junit.jupiter.api.Test

class FillEpisodeGapsUseCaseTest {

    private val useCase = FillEpisodeGapsUseCase()

    private fun episode(number: Int) =
        EpisodeInfo(number = number, title = "Ep $number", aired = null, isFiller = false, isRecap = false)

    private fun placeholder(number: Int) =
        EpisodeInfo(number = number, title = null, aired = null, isFiller = false, isRecap = false, isPlaceholder = true)

    @Test
    fun `returns list unchanged when episodeCount is null`() {
        val episodes = listOf(episode(1), episode(2))

        val result = useCase(episodes, episodeCount = null)

        assertThat(result).isEqualTo(episodes)
    }

    @Test
    fun `returns list unchanged when fetched count equals episodeCount`() {
        val episodes = listOf(episode(1), episode(2), episode(3))

        val result = useCase(episodes, episodeCount = 3)

        assertThat(result).isEqualTo(episodes)
    }

    @Test
    fun `returns list unchanged when fetched count exceeds episodeCount`() {
        val episodes = listOf(episode(1), episode(2), episode(3), episode(4))

        val result = useCase(episodes, episodeCount = 3)

        assertThat(result).isEqualTo(episodes)
    }

    @Test
    fun `fills trailing gap when last episodes are missing`() {
        val episodes = listOf(episode(1), episode(2))

        val result = useCase(episodes, episodeCount = 4)

        assertThat(result).containsExactly(
            episode(1), episode(2), placeholder(3), placeholder(4)
        ).inOrder()
    }

    @Test
    fun `fills leading gap when first episodes are missing`() {
        val episodes = listOf(episode(3), episode(4), episode(5))

        val result = useCase(episodes, episodeCount = 5)

        assertThat(result).containsExactly(
            placeholder(1), placeholder(2), episode(3), episode(4), episode(5)
        ).inOrder()
    }

    @Test
    fun `fills gap in the middle`() {
        val episodes = listOf(episode(1), episode(4))

        val result = useCase(episodes, episodeCount = 4)

        assertThat(result).containsExactly(
            episode(1), placeholder(2), placeholder(3), episode(4)
        ).inOrder()
    }

    @Test
    fun `fills all episodes when list is empty and episodeCount is known`() {
        val result = useCase(emptyList(), episodeCount = 1)

        assertThat(result).containsExactly(placeholder(1))
    }

    @Test
    fun `fills all episodes when list is empty and episodeCount is greater than one`() {
        val result = useCase(emptyList(), episodeCount = 3)

        assertThat(result).containsExactly(
            placeholder(1), placeholder(2), placeholder(3)
        ).inOrder()
    }

    @Test
    fun `output is sorted by episode number`() {
        val episodes = listOf(episode(3), episode(1))

        val result = useCase(episodes, episodeCount = 4)

        assertThat(result.map { it.number }).isEqualTo(listOf(1, 2, 3, 4))
    }

    @Test
    fun `placeholders have isPlaceholder true and real episodes have isPlaceholder false`() {
        val episodes = listOf(episode(1), episode(3))

        val result = useCase(episodes, episodeCount = 3)

        assertThat(result[0].isPlaceholder).isFalse()
        assertThat(result[1].isPlaceholder).isTrue()
        assertThat(result[2].isPlaceholder).isFalse()
    }
}
