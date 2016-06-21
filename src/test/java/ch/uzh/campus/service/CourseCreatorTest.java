package ch.uzh.campus.service;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Initial Date: 11.03.2016 <br>
 * 
 * @author lavinia
 */
public class CourseCreatorTest {

    @Test
    public void test() {
        String vvzLink = "http://www.vorlesungen.uzh.ch/FS16/suche/e-50778939.details.html";
        String objectives = "&lt;p&gt;&lt;img alt=&quot;&quot; height=&quot;25&quot; src=&quot;/olat/raw/_noversion_/images/campuslogo.png&quot; width=&quot;136&quot; /&gt;&lt;/p&gt; &lt;p&gt;Willkommen!&lt;/p&gt; &lt;p&gt;Informationen zu den Lehrveranstaltungen finden Sie im &lt;a href=&quot;http://www.vorlesungen.uzh.ch/&quot; target=&quot;_blank&quot;&gt;Vorlesungsverzeichnis&lt;/a&gt;.&lt;/p&gt; &lt;p&gt;Dieser Kurs wurde als &lt;span style=&quot;color: rgb(0,0,0);&quot;&gt;Campuskurs&lt;/span&gt; erstellt.     Wenn Sie das zugeh&amp;ouml;rige Modul gebucht haben, sind Sie hier     automatisch eingeschrieben worden.&lt;/p&gt;";
        String newObjective = objectives.replaceFirst("http://www.vorlesungen.uzh.ch/", vvzLink);
        assertTrue(!newObjective.isEmpty());
        assertTrue(newObjective.contains(vvzLink));
    }
}

