/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import junit.framework.TestCase;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import javax.swing.*;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class SearchEngineTextMatcherEditorTest extends TestCase {

    private FilterList<String> filterList;
    private JTextField textField = new JTextField();

    protected void setUp() throws Exception {
        filterList = new FilterList<String>(new BasicEventList<String>());
        filterList.addAll(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"));

        textField = new JTextField();
        filterList.setMatcherEditor(new SearchEngineTextMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator()));
    }

    public void testBasicFilter() {
        setNewFilter("Jo");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie Jocelyn"), filterList);

        setNewFilter("Ja");
        assertEquals(GlazedListsTests.delimitedStringToList("James"), filterList);

        setNewFilter("Jarek");
        assertTrue(filterList.isEmpty());

        setNewFilter("");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"), filterList);
    }

    public void testNegationFilter() {
        setNewFilter("Jo");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie Jocelyn"), filterList);

        setNewFilter("-Jo");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jimney"), filterList);

        setNewFilter("-JK");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"), filterList);

        setNewFilter("JK");
        assertTrue(filterList.isEmpty());

        setNewFilter("");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"), filterList);
    }

    public void testQuotedFilter() {
        setNewFilter("Jo die");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie"), filterList);

        setNewFilter("\"Jo die\"");
        assertTrue(filterList.isEmpty());

        setNewFilter("\"Jo die");
        assertTrue(filterList.isEmpty());

        setNewFilter("Jo die\"");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie"), filterList);
    }

    public void testFields() {
        final Customer jesse = new Customer("Jesse", "Wilson");
        final Customer james = new Customer("James", "Lemieux");
        final Customer holger = new Customer("Holger", "Brands");
        final Customer kevin = new Customer("Kevin", "Maltby");

        final FilterList<Customer> filteredCustomers = new FilterList<Customer>(new BasicEventList<Customer>());
        filteredCustomers.add(jesse);
        filteredCustomers.add(james);
        filteredCustomers.add(holger);
        filteredCustomers.add(kevin);

        final JTextField customerFilterField = new JTextField();
        final TextFilterator<Customer> customerFilterator = GlazedLists.textFilterator(Customer.class, new String[] {"firstName", "lastName"});
        Set<SearchEngineTextMatcherEditor.Field<Customer>> fields = new HashSet<SearchEngineTextMatcherEditor.Field<Customer>>(2);
        fields.add(new SearchEngineTextMatcherEditor.Field<Customer>("first", GlazedLists.textFilterator(Customer.class, new String[] {"firstName"})));
        fields.add(new SearchEngineTextMatcherEditor.Field<Customer>("last", GlazedLists.textFilterator(Customer.class, new String[] {"lastName"})));

        SearchEngineTextMatcherEditor<Customer> matcherEditor = new SearchEngineTextMatcherEditor<Customer>(customerFilterField, customerFilterator);
        matcherEditor.setFields(fields);
        filteredCustomers.setMatcherEditor(matcherEditor);

        assertEquals(Arrays.asList(new Customer[] {jesse, james, holger, kevin}), filteredCustomers);

        customerFilterField.setText("J");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(new Customer[] {jesse, james}), filteredCustomers);

        customerFilterField.setText("first:J");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(new Customer[] {jesse, james}), filteredCustomers);

        customerFilterField.setText("last:J");
        customerFilterField.postActionEvent();
        assertTrue(filteredCustomers.isEmpty());

        customerFilterField.setText("last:B");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(new Customer[] {holger, kevin}), filteredCustomers);

        customerFilterField.setText("first:e");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(new Customer[] {jesse, james, holger, kevin}), filteredCustomers);

        customerFilterField.setText("a");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(new Customer[] {james, holger, kevin}), filteredCustomers);

        matcherEditor.setFields(Collections.EMPTY_SET);
        customerFilterField.setText("first:e");
        customerFilterField.postActionEvent();
        assertTrue(filteredCustomers.isEmpty());
    }

    private void setNewFilter(String text) {
        textField.setText(text);
        textField.postActionEvent();
    }

    public static final class Customer {
        private final String firstName;
        private final String lastName;

        public Customer(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }
}