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
package uk.ac.ebi.intact.editor.converter;

import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.editor.controller.cvobject.CvObjectService;
import uk.ac.ebi.intact.model.CvObject;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@FacesConverter(value = "cvObjectConverter", forClass = CvObject.class)
public class CvObjectConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String ac) throws ConverterException {
        if (ac == null) return null;

        CvObjectService service = (CvObjectService) IntactContext.getCurrentInstance().getSpringContext().getBean("cvObjectService");
        return service.findCvObjectByAc(ac);
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) throws ConverterException {
        if (o == null) return null;
        
        if (o instanceof CvObject) {
            CvObject cvObject = (CvObject) o;
            return cvObject.getAc();
        } else {
            throw new IllegalArgumentException("Argument must be a CvObject: "+o+" ("+o.getClass()+")");
        }
    }
}