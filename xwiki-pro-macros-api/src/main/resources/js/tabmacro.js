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
const delay = (delayInms) => {
  return new Promise(resolve => setTimeout(resolve, delayInms));
};

require(['xwiki-page-ready', 'jquery'], function (pageReady, $) {
  async function processTabGroupElement(element) {

    let globalConfig = JSON.parse(element.getAttribute('data-config'));
    let elementToIterate = element.querySelectorAll("ul.nav-tabs li a");

    if (globalConfig["nextAfter"] > 0 && elementToIterate.length > 0) {
      for (let i = 0; i < 1 || globalConfig["loopCards"]; i++) {
        for (const el of elementToIterate) {
          let divElement = element.querySelector("#" + el.getAttribute("aria-controls"))
          let tabConfig = JSON.parse(divElement.getAttribute('data-config'))
          if (tabConfig["nextAfter"] <= 0) {
            // should not happen but in case avoid to have an infinite loop
            return
          }
          $(el).tab('show')
          await delay(tabConfig["nextAfter"] * 1000)
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