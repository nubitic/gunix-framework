package mx.com.gunix.framework.ui.springmvc.tiles3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.tiles.Attribute;
import org.apache.tiles.Definition;
import org.apache.tiles.impl.BasicTilesContainer;
import org.apache.tiles.request.Request;
import org.springframework.util.Assert;

public class AjaxTilesView extends org.springframework.js.ajax.tiles3.AjaxTilesView {

	@Override
	protected void flattenAttributeMap(BasicTilesContainer container, Request tilesRequest, Map<String, Attribute> resultMap, Definition definition) {
		Set<String> attributeNames = new HashSet<String>();
		if (definition.getLocalAttributeNames() != null) {
			attributeNames.addAll(definition.getLocalAttributeNames());
		}
		if (definition.getCascadedAttributeNames() != null) {
			attributeNames.addAll(definition.getCascadedAttributeNames());
		}
		for (String attributeName : attributeNames) {
			Attribute attribute = definition.getAttribute(attributeName);
			if (attribute.getExpressionObject() == null && (attribute.getValue() == null || !(attribute.getValue() instanceof String))) {
				continue;
			}
			if (attribute.getExpressionObject() == null) {
				String value = attribute.getValue().toString();
				if (value.startsWith("/")) {
					resultMap.put(attributeName, attribute);
				} else if (container.isValidDefinition(value, tilesRequest)) {
					resultMap.put(attributeName, attribute);
					Definition nestedDefinition = container.getDefinitionsFactory().getDefinition(value, tilesRequest);
					Assert.isTrue(nestedDefinition != definition, "Circular nested definition: " + value);
					flattenAttributeMap(container, tilesRequest, resultMap, nestedDefinition);
				}
			} else {
				resultMap.put(attributeName, attribute);
			}
		}

	}

}
