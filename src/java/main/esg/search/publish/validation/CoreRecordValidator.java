package esg.search.publish.validation;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.springframework.util.StringUtils;

import esg.search.core.Record;
import esg.search.core.RecordImpl;
import esg.search.utils.XmlParser;

/**
 * Class that validates incoming XML/Solr records before ingestion.
 * This class validates the few required core metadata fields.
 * 
 * @author Luca Cinquini
 *
 */
public class CoreRecordValidator implements RecordValidator {
    
    // The underlying XML parser
    XmlParser parser = new XmlParser(false); // validate XSD = false
    
    // Xpath expressions to locate specific XML elements
    private final XPath idPath;
    private final XPath typePath;
    private final XPath titlePath;
    
    public CoreRecordValidator() throws Exception {
        idPath = XPath.newInstance("/doc/field[@name='id']");
        typePath = XPath.newInstance("/doc/field[@name='type']");
        titlePath = XPath.newInstance("/doc/field[@name='title']");
    }

    @Override
    public Record validate(String xml) throws Exception {
        
        Document doc = parser.parseString(xml);
        
        // record identifier
        Element idElement = (Element)idPath.selectSingleNode(doc);
        if (idElement==null || !StringUtils.hasText(idElement.getTextNormalize())) {
            throw new Exception("Missing record identifier");
        }
        
        // record type
        Element typeElement = (Element)typePath.selectSingleNode(doc);
        if (typeElement==null || !StringUtils.hasText(typeElement.getTextNormalize())) {
            throw new Exception("Missing record type");
        }
        
        // record title
        Element titleElement = (Element)titlePath.selectSingleNode(doc);
        if (titleElement==null || !StringUtils.hasText(titleElement.getTextNormalize())) {
            throw new Exception("Missing record title");
        }
        
        // build stub record object
        Record record = new RecordImpl(idElement.getTextNormalize());
        record.setType(typeElement.getTextNormalize());
        return record;
        
    }
}
