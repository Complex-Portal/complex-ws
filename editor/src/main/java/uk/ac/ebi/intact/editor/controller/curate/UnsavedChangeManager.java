/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller.curate;

import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.model.IntactObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class UnsavedChangeManager {

    private List<UnsavedChange> changes;
    private boolean unsavedChanges;

    public UnsavedChangeManager() {
        changes = new ArrayList<UnsavedChange>();
    }

    public void markToDelete(IntactObject object) {
        unsavedChanges = true;
        changes.add(new UnsavedChange(object, UnsavedChange.DELETED));
    }

    public boolean isDeletedAc(String ac) {
        return getDeletedAcs(IntactObject.class).contains(ac);
    }

    public List<String> getDeletedAcs(Class type) {
        return DebugUtil.acList(getDeleted(type));
    }

    public List<String> getDeletedAcsByClassName(String className) {
        try {
            return getDeletedAcs(Thread.currentThread().getContextClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public List<IntactObject> getDeleted(Class type) {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : changes) {
            if (UnsavedChange.DELETED.equals(change.getAction()) &&
                    type.isAssignableFrom(change.getUnsavedObject().getClass())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<IntactObject> getAllDeleted() {
        List<IntactObject> ios = new ArrayList<IntactObject>();

        for (UnsavedChange change : changes) {
            if (UnsavedChange.DELETED.equals(change.getAction())) {
                IntactObject intactObject = change.getUnsavedObject();
                ios.add(intactObject);
            }
        }

        return ios;
    }

    public List<UnsavedChange> getChanges() {
        return changes;
    }

    public void clearChanges() {
        changes.clear();
    }

    public boolean isUnsavedChanges() {
        return unsavedChanges;
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        this.unsavedChanges = unsavedChanges;
    }
}
