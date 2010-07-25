/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.view;

import java.util.LinkedList;

public class Menu
        extends LinkedList<Menu.MenuItem> {

    private static final long serialVersionUID = 1L;

    public void addMenuItem(String name, String href, String messageKey) {
        add(new MenuItem(name, href, messageKey));
    }

    public class MenuItem {

        private String name;

        private String href;

        private String messageKey;

        protected MenuItem(String name, String href, String messageKey) {
            super();
            this.name = name;
            this.href = href;
            this.messageKey = messageKey;
        }

        public Object getName() {
            return this.name;
        }

        public String getHref() {
            return href;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }
}
