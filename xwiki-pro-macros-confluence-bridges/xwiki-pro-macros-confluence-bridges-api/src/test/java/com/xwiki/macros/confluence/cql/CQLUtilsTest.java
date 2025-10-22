/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.macros.confluence.cql;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xwiki.macros.confluence.internal.ConfluenceSpaceUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CQLUtils} to make sure that we build valid CQLs.
 */
@ComponentTest
public class CQLUtilsTest
{
    @InjectMockComponents
    private CQLUtils cqlUtils;

    @MockComponent
    private ConfluenceSpaceUtils confluenceSpaceUtils;

    @MockComponent
    private EntityReferenceSerializer<String> serializer;

    @MockComponent
    private Logger logger;

    @Test
    void buildQuery()
    {
        Map<String, Object> map = new HashMap<>();
        // Build the query from an empty map.
        String query = cqlUtils.buildQuery(map);
        assertEquals("", query);
        // Check that the label is taken into account if is the only parameter.
        map.put("label", "test");
        assertEquals("(label = \"test\")", cqlUtils.buildQuery(map));
        // Check that the labels parameters takes precedes over the label, if present.
        map.put("labels", "test2");
        assertEquals("(label = \"test2\")", cqlUtils.buildQuery(map));
        // Check that multiple labels are taken into account if given and that the default operator is OR
        map.put("labels", "l1, l2,   l3,l4,   l5");
        assertEquals("(label = \"l1\" OR label = \"l2\" OR label = \"l3\" OR label = \"l4\" OR label = \"l5\")",
            cqlUtils.buildQuery(map));
        // Check that the query takes into account the operator.
        map.put("operator", "AND");
        map.put("labels", "l1, l2");
        assertEquals("(label = \"l1\" AND label = \"l2\")", cqlUtils.buildQuery(map));
        // Check that the type parameter is taken into account if present.
        map.put("type", "page");
        assertEquals("(label = \"l1\" AND label = \"l2\") AND type = \"page\"", cqlUtils.buildQuery(map));
    }

    @Test
    void builderFilterWithEmptySpaces()
    {
        // Should return an empty string when spaces are empty or null.
        assertEquals("", cqlUtils.builderFilter(""));
        assertEquals("", cqlUtils.builderFilter("   "));
        assertEquals("", cqlUtils.builderFilter("@all"));
    }

    @Test
    void builderFilterWithSingleSpace()
    {
        EntityReference ref = mock(EntityReference.class);
        when(confluenceSpaceUtils.getSloppySpace("DEV")).thenReturn(ref);
        when(ref.getRoot()).thenReturn(new EntityReference("wiki", EntityType.WIKI));
        when(ref.getReversedReferenceChain()).thenReturn(java.util.List.of(new EntityReference("wiki", EntityType.WIKI), new EntityReference("DEV", EntityType.SPACE)));
        when(serializer.serialize(ref)).thenReturn("wiki:DEV");

        String result = cqlUtils.builderFilter("DEV");
        assertEquals(" space_facet:0\\/wiki\\:DEV", result);
    }

    @Test
    void builderFilterWithMultipleSpaces()
    {
        EntityReference ref1 = mock(EntityReference.class);
        EntityReference ref2 = mock(EntityReference.class);

        when(confluenceSpaceUtils.getSloppySpace("DEV")).thenReturn(ref1);
        when(confluenceSpaceUtils.getSloppySpace("Q+A")).thenReturn(ref2);

        when(ref1.getRoot()).thenReturn(new EntityReference("wiki", EntityType.WIKI));
        when(ref2.getRoot()).thenReturn(new EntityReference("wiki", EntityType.WIKI));

        when(ref1.getReversedReferenceChain()).thenReturn(java.util.List.of(
            new EntityReference("wiki", EntityType.WIKI),
            new EntityReference("DEV", EntityType.SPACE)));
        when(ref2.getReversedReferenceChain()).thenReturn(java.util.List.of(
            new EntityReference("wiki", EntityType.WIKI),
            new EntityReference("Q+A", EntityType.SPACE)));

        when(serializer.serialize(ref1)).thenReturn("wiki:DEV");
        when(serializer.serialize(ref2)).thenReturn("wiki:Q+A");

        String result = cqlUtils.builderFilter("DEV, Q+A");
        assertEquals(" space_facet:0\\/wiki\\:DEV space_facet:0\\/wiki\\:Q\\+A", result);
    }
}

