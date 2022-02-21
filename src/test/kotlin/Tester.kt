import com.newtranx.eval.enum.Language
import com.newtranx.eval.metrics.MetricUtil
import com.newtranx.eval.metrics.nltk.Meteor
import org.junit.Before
import org.junit.Test

/**
 * @Author: anson
 * @Date: 2022/2/18 11:16 PM
 */
class Tester {

    companion object {
        val hypothesis = listOf("我是中国人", "我爱吃水果")
        private val ref1 = listOf("我是中国人", "我爱水果")
        private val ref2 = listOf("中国是我", "我爱吃水果")
        val language = Language.ZH
        val references = listOf(ref1, ref2)
        val path = Meteor::class.java.getResource("/wordnet").path
        val wordnet = MetricUtil.buildWordnet(path)
    }

    @Before
    fun prepare() {
    }

    @Test
    fun testBleu() {
        val bleu = MetricUtil.buildBleuMetric(language)
        val score = bleu.corpusScore(hypothesis, references)
        println(score)
    }

    @Test
    fun testTer() {
        val ter = MetricUtil.buildTerMetric(normalized = true, asianSupport = true)
        val score = ter.corpusScore(hypothesis, references)
        println(score)
    }

    @Test
    fun testNist() {
        val nist = MetricUtil.buildNistMetric(asianSupport = true)
        val score = nist.corpusScore(hypothesis, references)
//        val score = nist.corpusScore(listOf("It is a guide to action"), listOf(listOf("It is a guide to action"), listOf("It is the guiding principle which")))
        println(score)
    }

    @Test
    fun testMeteor() {
        val meteor = MetricUtil.buildMeteorMetric(wordnet, language)
        val score = meteor.corpusScore(hypothesis, references)
        println(score)
    }

    @Test
    fun testMeteorSingle() {
        val meteor = Meteor(wordnet = wordnet)
        val score = meteor.singleMeteorScore(
            "It is a guide to action which ensures that the military always obeys the commands of the party",
            "It is a guide to action which ensures that the military always obeys the commands of the party"
        )
        println(score)
    }
}