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
package com.xwiki.pro.test.po.confluence.viewfile;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.xwiki.test.ui.po.BaseElement;
import org.xwiki.test.ui.po.SuggestInputElement;

/**
 * The macro editor modal of the View File macro, giving access to its page and attachment pickers.
 * <p>
 * This doesn't extend the platform {@code MacroDialogEditModal} because it moved from the CKEditor test page objects
 * to the WYSIWYG test page objects in XWiki 18, and we want to support all the XWiki versions starting with 15.10.
 *
 * @version $Id$
 * @since 1.33.1
 */
public class ViewFileMacroEditModal extends BaseElement
{
    private static final By MACRO_NAME = By.cssSelector(".macro-editor-modal .macro-name");

    private static final By SUBMIT_BUTTON = By.cssSelector(".macro-editor-modal .modal-footer .btn-primary");

    private static final By PAGE_PICKER = By.cssSelector(".macro-editor-modal select[name='page']");

    private static final By FILE_NAME_PICKER = By.cssSelector(".macro-editor-modal input[name='name']");

    private static final By COLLAPSED_OPTIONAL_PARAMETERS =
        By.cssSelector(".macro-editor-modal .more .arrow-right");

    private static final By OPTIONAL_PARAMETERS_TOGGLE = By.cssSelector(".macro-editor-modal .more a");

    /**
     * Wait until the macro editor is loaded and the attachment picker is ready.
     *
     * @return this modal
     */
    public ViewFileMacroEditModal waitUntilReady()
    {
        getDriver().waitUntilElementIsVisible(MACRO_NAME);
        showOptionalParameters();
        // The attachment picker is initialized asynchronously by the View File macro JavaScript.
        getFileNamePicker();
        return this;
    }

    /**
     * Submit the macro editor.
     */
    public void clickSubmit()
    {
        getDriver().findElement(SUBMIT_BUTTON).click();
    }

    /**
     * Some XWiki versions hide the optional macro parameters (among which the page and the file name) behind a "More"
     * toggle.
     */
    private void showOptionalParameters()
    {
        if (!getDriver().findElementsWithoutWaiting(COLLAPSED_OPTIONAL_PARAMETERS).isEmpty()) {
            getDriver().findElementWithoutWaiting(OPTIONAL_PARAMETERS_TOGGLE).click();
            getDriver().waitUntilCondition(
                driver -> getDriver().findElementsWithoutWaiting(COLLAPSED_OPTIONAL_PARAMETERS).isEmpty());
        }
    }

    /**
     * Select the page from which the attachments are taken, using the page picker.
     *
     * @param query the text to type in the page picker
     * @param pageTitle the title of the page to select among the suggestions
     * @return this modal
     */
    public ViewFileMacroEditModal setPage(String query, String pageTitle)
    {
        WebElement fileNameInput = getDriver().findElement(FILE_NAME_PICKER);
        SuggestInputElement pagePicker = new SuggestInputElement(getDriver().findElement(PAGE_PICKER));
        pagePicker.click().sendKeys(query);
        selectSuggestion(pagePicker, pageTitle);
        // Changing the page re-creates the attachment picker, so that it suggests the attachments of the new page.
        getDriver().waitUntilCondition(ExpectedConditions.stalenessOf(fileNameInput));
        getFileNamePicker();
        return this;
    }

    /**
     * Open the attachment picker and wait for its suggestions. The selected attachment is suggested right away, so we
     * wait for the search results, i.e. for at least one suggestion that is not the selected attachment.
     *
     * @return the names of the suggested attachments
     */
    public List<String> getFileNameSuggestions()
    {
        SuggestInputElement fileNamePicker = getFileNamePicker();
        List<String> selectedValues = fileNamePicker.getSelectedSuggestions().stream()
            .map(SuggestInputElement.SuggestionElement::getValue).collect(Collectors.toList());
        fileNamePicker.click();
        getDriver().waitUntilCondition(driver -> fileNamePicker.waitForSuggestions().getSuggestions().stream()
            .anyMatch(suggestion -> !selectedValues.contains(suggestion.getValue())));
        return fileNamePicker.getSuggestions().stream().map(SuggestInputElement.SuggestionElement::getLabel)
            .collect(Collectors.toList());
    }

    /**
     * Select an attachment using the attachment picker.
     *
     * @param fileName the name of the attachment to select
     * @return this modal
     */
    public ViewFileMacroEditModal setFileName(String fileName)
    {
        SuggestInputElement fileNamePicker = getFileNamePicker().click();
        selectSuggestion(fileNamePicker, fileName);
        return this;
    }

    /**
     * @return the name of the selected attachment, as displayed by the attachment picker
     */
    public String getFileName()
    {
        return getFileNamePicker().getSelectedSuggestions().get(0).getLabel();
    }

    private SuggestInputElement getFileNamePicker()
    {
        // The constructor waits for the input to be enhanced into a picker.
        return new SuggestInputElement(getDriver().findElement(FILE_NAME_PICKER));
    }

    private void selectSuggestion(SuggestInputElement picker, String label)
    {
        SuggestInputElement.SuggestionElement suggestion = getDriver().waitUntilCondition(
            driver -> picker.waitForSuggestions().getSuggestions().stream()
                .filter(item -> label.equals(item.getLabel())).findFirst().orElse(null));
        suggestion.select();
        picker.waitForSuggestionsClearance();
    }
}
