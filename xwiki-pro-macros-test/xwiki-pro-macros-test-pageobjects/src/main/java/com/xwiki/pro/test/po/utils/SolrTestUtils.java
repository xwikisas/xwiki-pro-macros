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
package com.xwiki.pro.test.po.utils;

import org.openqa.selenium.By;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.ui.TestUtils;

/**
 * Utility class to wait for the Solr indexing.
 *
 * @version $Id$
 * @since 1.27.2
 */
public class SolrTestUtils
{
    private static final String SOLRSERVICE_SPACE = "TestService";

    private static final String SOLRSERVICE_PAGE = "Solr";

    private static final DocumentReference documentReference =
        new DocumentReference("xwiki", SOLRSERVICE_SPACE, SOLRSERVICE_PAGE);

    private final TestUtils testUtils;

    public SolrTestUtils(TestUtils testUtils) throws Exception
    {
        this.testUtils = testUtils;
        initService();
    }

    public void waitEmpyQueue() throws Exception
    {
        while (getSolrQueueSize() > 0) {
            Thread.sleep(100);
        }
    }

    private long getSolrQueueSize() throws Exception
    {
        testUtils.gotoPage(documentReference);
        return Integer.parseInt(testUtils.getDriver().findElement(By.id("solr")).getText());
    }

    private void initService() throws Exception
    {

        testUtils.createPage(documentReference,
            "(% id = \"solr\" %)\n" + "(((\n" + "{{velocity}}$services.solr.queueSize{{/velocity}}\n" + ")))");
    }
}
