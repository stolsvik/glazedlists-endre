/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import junit.framework.TestCase;

public class UnicodeTextMatcherEditorTest extends TestCase {

    public void testUnicodeStrategy() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        textMatcherEditor.setStrategy(GlazedListsICU4J.UNICODE_TEXT_SEARCH_STRATEGY);
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);

        list.add(null);
        list.add("");
        list.add("r\u00e9sum\u00e9");
        list.add("Bj\u00f6rk");
        list.add("M\u00fcller");
        list.add("\u00c6nima"); // �nima
        list.add("Ru\u00dfland"); // Ru�land

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"M\u00fcller"});
        assertEquals(1, list.size());
        assertEquals("M\u00fcller", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"M\u00fcLLER"});
        assertEquals(1, list.size());
        assertEquals("M\u00fcller", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"Muller"});
        assertEquals(1, list.size());
        assertEquals("M\u00fcller", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"MULLER"});
        assertEquals(1, list.size());
        assertEquals("M\u00fcller", list.get(0));


        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"\u00c6nima"}); // �nima
        assertEquals(1, list.size());
        assertEquals("\u00c6nima", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"\u00e6nima"}); // �nima
        assertEquals(1, list.size());
        assertEquals("\u00c6nima", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"Aenima"});
        assertEquals(1, list.size());
        assertEquals("\u00c6nima", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"aenima"});
        assertEquals(1, list.size());
        assertEquals("\u00c6nima", list.get(0));
    }

    public void testUnicodeStrategy_StartsWith_FixMe() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        textMatcherEditor.setStrategy(GlazedListsICU4J.UNICODE_TEXT_SEARCH_STRATEGY);
        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);

        list.add("Ru\u00dfland"); // Ru�land

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"Ru\u00df"});
        assertEquals(1, list.size());
        assertEquals("Ru\u00dfland", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"\u00dfland"});
        assertTrue(list.isEmpty());

        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        assertEquals(1, list.size());
        assertEquals("Ru\u00dfland", list.get(0));

        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        assertTrue(list.isEmpty());

        // todo this highlights a bug in ICU4J, not Glazed Lists. We should update our
        // icu4j.jar on glazedlists.dev.java.net once this bug has been fixed:
        // http://bugs.icu-project.org/trac/ticket/5420
        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"Russland"});
        assertEquals(1, list.size());
        assertEquals("Ru\u00dfland", list.get(0));
    }
}