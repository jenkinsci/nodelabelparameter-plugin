/*
 * The MIT License
 *
 * Copyright 2024 Mark Waite.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import hudson.slaves.DumbSlave;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.AllNodeEligibility;
import org.jvnet.jenkins.plugins.nodelabelparameter.node.NodeEligibility;

public class LabelParameterValueTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static DumbSlave agent;

    @BeforeClass
    public static void createAgent() throws Exception {
        agent = j.createOnlineSlave();
    }

    private final Random random = new Random();

    public LabelParameterValueTest() {}

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(LabelParameterValue.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .withIgnoredFields("description", "nextLabels")
                .verify();
    }

    @Test
    @Deprecated
    public void testLabelParameterValueDeprecated2ArgConstructor() {
        String value = " my-label "; // Intentionally has leading and trailing spaces
        String trimmedValue = value.trim();
        // Use either value or trimmedValue randomly, does not change any assertion
        LabelParameterValue labelParameterValue =
                new LabelParameterValue("my-name", random.nextBoolean() ? value : trimmedValue);
        assertThat(labelParameterValue.getName(), is("my-name"));
        assertThat(labelParameterValue.getLabel(), is(trimmedValue));
        assertThat(labelParameterValue.getDescription(), is(nullValue()));
        assertThat(labelParameterValue.getNextLabels(), is(empty()));
    }

    @Test
    @Deprecated
    public void testLabelParameterValueDeprecated2ArgConstructorNullName() {
        String value = " my-label "; // Intentionally has leading and trailing spaces
        String trimmedValue = value.trim();
        // Use either value or trimmedValue randomly, does not change any assertion
        LabelParameterValue labelParameterValue =
                new LabelParameterValue(null, random.nextBoolean() ? value : trimmedValue);
        assertThat(labelParameterValue.getName(), is("NODELABEL"));
        assertThat(labelParameterValue.getLabel(), is(trimmedValue));
        assertThat(labelParameterValue.getDescription(), is(nullValue()));
        assertThat(labelParameterValue.getNextLabels(), is(empty()));
    }

    @Test
    @Deprecated
    public void testLabelParameterValueDeprecated3ArgConstructor() {
        String value = " my-label "; // Intentionally has leading and trailing spaces
        String trimmedValue = value.trim();
        String name = "my-name";
        String description = "My description";
        // Use either value or trimmedValue randomly, does not change any assertion
        LabelParameterValue labelParameterValue =
                new LabelParameterValue(name, description, random.nextBoolean() ? value : trimmedValue);
        assertThat(labelParameterValue.getName(), is(name));
        assertThat(labelParameterValue.getLabel(), is(trimmedValue));
        assertThat(labelParameterValue.getDescription(), is(description));
        assertThat(labelParameterValue.getNextLabels(), is(empty()));
    }

    @Test
    public void testGetNextLabels() {
        String name = "my-name";
        List<String> extraNodeNames = Arrays.asList("built-in", "not-a-valid-node-name");
        List<String> nodeNames = new ArrayList<>();
        nodeNames.add(agent.getNodeName());
        nodeNames.addAll(extraNodeNames);
        NodeEligibility eligibility = new AllNodeEligibility();
        LabelParameterValue labelParameterValue = new LabelParameterValue(name, nodeNames, eligibility);
        assertThat(labelParameterValue.getName(), is(name));
        assertThat(labelParameterValue.getLabel(), is(agent.getNodeName()));
        assertThat(labelParameterValue.getNextLabels(), is(extraNodeNames));
    }
}
