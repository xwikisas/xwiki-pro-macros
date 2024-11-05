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
require(['xwiki-page-ready', 'jquery'], function (pageReady, $) {
  async function processTabGroupElement(element) {

    let globalNextAfter = parseInt(element.getAttribute('data-nextafter'));
    let loopCards = element.getAttribute('data-loopcards');
    let elementToIterate = element.querySelectorAll("ul.nav-tabs li a");

    if (!isNaN(tabNextAfter) && globalNextAfter > 0 && elementToIterate.length > 0) {
      for (let i = 0; i < 1 || loopCards === "true"; i++) {
        for (const el of elementToIterate) {
          let divElement = element.querySelector("#" + el.getAttribute("aria-controls"))
          let tabNextAfter = parseInt(divElement.getAttribute('data-nextafter'))
          if (isNaN(tabNextAfter) || tabNextAfter <= 0) {
            tabNextAfter = globalNextAfter;
          }
          $(el).tab('show')
          await new Promise(resolve => setTimeout(resolve, tabNextAfter * 1000));
        }
      }
    }
  }

  pageReady.afterPageReady(() => {
    let tabMacroElements = document.querySelectorAll('div.xwikitabmacro');
    try {
      let promises = tabMacroElements.values().map(i => processTabGroupElement(i));
      Promise.all(promises).catch((err) => console.log(err));
    }
    catch (err) {
      console.error(err);
    }
  });
});