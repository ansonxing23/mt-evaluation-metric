package com.newtranx.eval.metrics.sacre

import com.newtranx.eval.metrics.TerScore
import com.newtranx.eval.tokenizers.TokenizerTer
import com.newtranx.eval.utils.sumOfLists
import com.newtranx.eval.utils.translationEditRate

/**
 * @Author: anson
 * @Date: 2022/2/1 4:26 PM
 *
 * Translation edit rate (TER). A near-exact reimplementation of the Tercom
 * algorithm, produces identical results on all "sane" outputs.
 *
 * Tercom original implementation: https://github.com/jhclark/tercom
 *
 * The beam edit distance algorithm uses a slightly different approach (we stay
 * around the diagonal which is faster, at least in Python) so in some
 * (extreme) corner cases, the output could differ.
 *
 * Caching in the edit distance is based partly on the PyTer package by Hiroyuki
 * Tanaka (MIT license). (https://github.com/aflc/pyter)
 *
 * @param normalized: If `True`, applies basic tokenization to sentences.
 * @param noPunct: If `True`, removes punctuations from sentences.
 * @param asianSupport: If `True`, adds support for Asian character processing.
 * @param caseSensitive: If `True`, does not lowercase sentences.
 */
class Ter @JvmOverloads constructor(
    val normalized: Boolean = false,
    val noPunct: Boolean = false,
    val asianSupport: Boolean = false,
    val caseSensitive: Boolean = false
) : Base() {
    private var tokenizer = TokenizerTer(
        normalized = this.normalized,
        noPunct = this.noPunct,
        asianSupport = this.asianSupport,
        caseSensitive = this.caseSensitive
    )

    override fun aggregateAndCompute(stats: List<List<Double>>, sentenceLevel: Boolean): TerScore {
        return computeScoreFromStats(sumOfLists(stats))
    }

    /**
     * Computes the final score from already aggregated statistics.
     *
     * @param stats: A list or numpy array of segment-level statistics.
     * @return: A `TERScore` object.
     */
    private fun computeScoreFromStats(stats: List<Double>): TerScore {
        val totalEdits = stats[0]
        val sumRefLengths = stats[1]
        val score = (totalEdits / sumRefLengths).takeIf { sumRefLengths > 0 } ?: 1.0
        return TerScore(100 * score, totalEdits, sumRefLengths)
    }

    override fun preprocessSegment(sent: String): String {
        return tokenizer.parse(sent.trim())
    }

    /**
     * Given a (pre-processed) hypothesis sentence and already computed
     * reference words, returns the segment statistics required to compute
     * the full TER score.
     *
     * @param hypothesis: Hypothesis sentence.
     * @param refKwargs: A dictionary with `ref_words` key which is a list
     * where each sublist contains reference words.
     * @return A two-element list that contains the 'minimum number of edits'
     * and 'the average reference length'.
     */
    override fun computeSegmentStatistics(hypothesis: String, refKwargs: SegmentStatistics): List<Double> {
        var refLengths = 0
        var bestNumEdits = Float.MAX_VALUE

        val wordsHyp = hypothesis.split(" ")

        // Iterate the references
        val refWords = refKwargs.refWords

        refWords.forEach { words_ref ->
            val rate = translationEditRate(wordsHyp, words_ref)
            val numEdits = rate.totalEdits
            val refLen = rate.nWordsRef
            refLengths += refLen
            if (numEdits < bestNumEdits) {
                bestNumEdits = numEdits.toFloat()
            }
        }
        val avgRefLen = refLengths.toDouble() / refWords.size
        return listOf(bestNumEdits.toDouble(), avgRefLen)
    }

    /**
     * Given a list of reference segments, applies pre-processing & tokenization
     * and returns list of tokens for each reference.
     *
     * @param refs: A sequence of strings.
     * @return A dictionary that will be passed to `_compute_segment_statistics()`
     * through keyword arguments.
     */
    override fun extractReferenceInfo(refs: List<String>): SegmentStatistics {
        val refWords = refs.map { ref ->
            tokenizer.rawParse(ref.trim())
        }
        return SegmentStatistics(
            refWords = refWords
        )
    }

}
