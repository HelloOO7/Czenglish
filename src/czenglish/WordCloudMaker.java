package czenglish;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.slf4j.Log4jLogger;

public class WordCloudMaker {

	public static WordCloud makeWordCloud(Collection<OccurenceGroup> words, int limit) {
		List<OccurenceGroup> l = new ArrayList<>(words);

		l.sort(new Comparator<OccurenceGroup>() {
			@Override
			public int compare(OccurenceGroup o1, OccurenceGroup o2) {
				return o2.numOccurences - o1.numOccurences;
			}
		});
		l = l.subList(0, Math.min(l.size(), limit));
		int minOccurences = l.get(l.size() - 1).numOccurences - 1;
		for (OccurenceGroup g : l) {
			g.numOccurences -= minOccurences;
		}

		//Configurator.setLevel("com.kennycason.kumo.WordCloud", org.apache.logging.log4j.Level.ALL);

		final List<WordFrequency> wordFrequencies = new ArrayList<>();

		for (OccurenceGroup grp : l) {
			WordFrequency freq = new WordFrequency(grp.getMostOccurText(), grp.numOccurences);
			wordFrequencies.add(freq);
		}

		final Dimension dimension = new Dimension(1080, 1080);
		final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		wordCloud.setKumoFont(new KumoFont(new Font("Consolas", Font.PLAIN, 50)));
		wordCloud.setBackground(new CircleBackground(540));
		wordCloud.setColorPalette(new ColorPalette(makeSpectrum()));
		wordCloud.setFontScalar(new SqrtFontScalar(10, 100));
		wordCloud.build(wordFrequencies);
		return wordCloud;
	}

	public static List<Color> makeSpectrum() {
		List<Color> l = new ArrayList<>();

		for (float h = 0.3f; h < 1f; h += 0.01f) {
			l.add(Color.getHSBColor(h, 0.5f, 0.8f));
		}
		Collections.shuffle(l);

		return l;
	}
}
