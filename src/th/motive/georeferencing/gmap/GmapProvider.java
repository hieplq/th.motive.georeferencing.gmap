package th.motive.georeferencing.gmap;

import java.util.List;
import org.compiere.model.MLocation;
import org.compiere.model.MSysConfig;
import org.compiere.util.CLogger;
import org.compiere.util.Util;
import org.osgi.service.component.annotations.Component;

import de.bxservice.georeferencing.tools.AbstractGeoreferencingHelper;
import de.bxservice.georeferencing.tools.DefaultMarkersGeojson;
import de.bxservice.georeferencing.tools.IGeoreferencingHelper;

@Component(
		service = IGeoreferencingHelper.class,
		property= {"helperType:String=th.motive.gmap"}
)
public class GmapProvider extends AbstractGeoreferencingHelper{

	/**	Logger			*/
	protected CLogger log = CLogger.getCLogger(GmapProvider.class);

	/**	Mapbox Access Token		*/
	private String API_KEY = MSysConfig.getValue("GMAP_API_KEY");

	private DefaultMarkersGeojson markersGeojsonParse = new DefaultMarkersGeojson();

	@Override
	public String getMapMarkers() {
		if (Util.isEmpty(API_KEY)) {
			log.warning("No Gmap API Key configured");
			return "MISSING API KEY";
		}
		StringBuilder fragmentHtml = new StringBuilder();
		
		fragmentHtml.append("<!DOCTYPE html>\n<html>\n<head>\n");
		fragmentHtml.append("	<meta charset='utf-8'/>\n");
		fragmentHtml.append("	<meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />\n");
		fragmentHtml.append("	<style>\n");
		fragmentHtml.append("		#map {\n");
		fragmentHtml.append("			width: 100%;\n");
		fragmentHtml.append("			height:100%;\n");
		fragmentHtml.append("			background-color: grey;\n");
		fragmentHtml.append("		}\n");
		fragmentHtml.append("html, body {\n");
		fragmentHtml.append("  height: 100%;\n");
		fragmentHtml.append("  margin: 0;\n");
		fragmentHtml.append("  padding: 0;\n");
		fragmentHtml.append("}\n");
		fragmentHtml.append("	</style>\n");
		fragmentHtml.append("	<script>\n");
		fragmentHtml.append(markersGeojsonParse.buildMarkersAsGeojson(mapMarkers, "markerData")).append("\n");
		
		//https://jsfiddle.net/hieplq/vt60o7Le/93/
		fragmentHtml.append("function initMap() {\n");
		fragmentHtml.append("	//https://developers.google.com/maps/documentation/javascript/reference/map\n");
		fragmentHtml.append("  map = new google.maps.Map(\n");
		fragmentHtml.append("      	document.getElementById('map'),\n");
		fragmentHtml.append("      	{center: new google.maps.LatLng(").append(initialLongitude).append(", ").append(initialLatitude).append("),\n");
		fragmentHtml.append("      	zoom:").append(zoomValue).append(",\n");
		fragmentHtml.append("        mapTypeId:google.maps.MapTypeId.SATELLITE,\n");
		fragmentHtml.append("        clickableIcons:true,\n");
		fragmentHtml.append("        keyboardShortcuts:false,\n");
		fragmentHtml.append("        streetViewControl: true,\n");
		fragmentHtml.append("        streetViewControlOptions: {position: google.maps.ControlPosition.RIGHT_CENTER},\n");
		fragmentHtml.append("        zoomControl: true,\n");
		fragmentHtml.append("        zoomControlOptions: {position: google.maps.ControlPosition.RIGHT_BOTTOM},\n");
		fragmentHtml.append("        fullscreenControl:true,\n");
		fragmentHtml.append("        fullscreenControlOptions:{position:google.maps.ControlPosition.BOTTOM_RIGHT},\n");
		fragmentHtml.append("			  scaleControl: true,\n");
		fragmentHtml.append("        scaleControlOptions:{style:google.maps.ScaleControlStyle.DEFAULT},\n");
		fragmentHtml.append("			  rotateControl: true,\n");
		fragmentHtml.append("        rotateControlOptions: {position: google.maps.ControlPosition.BOTTOM_RIGHT},\n");
		fragmentHtml.append("        mapTypeControl:true,\n");
		fragmentHtml.append("        mapTypeControlOptions:{mapTypeIds:[google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.HYBRID]}\n");
		fragmentHtml.append("        }\n");
		fragmentHtml.append("      );\n");
		fragmentHtml.append("\n");		
		fragmentHtml.append("	map.data.setStyle(function(feature) {\n");
		fragmentHtml.append("    var ascii = feature.getProperty('ascii');\n");
		fragmentHtml.append("    var color = ascii > 91 ? 'red' : 'blue';\n");
		fragmentHtml.append("    return {\n");
		fragmentHtml.append("      icon: \"http://maps.google.com/mapfiles/ms/icons/\" + feature.getProperty(\"mcolor\") + \"-dot.png\",\n");
		fragmentHtml.append("      //icon: \"https://maps.google.com/mapfiles/kml/paddle/2.png\",\n");
		fragmentHtml.append("      strokeWeight: 1\n");
		fragmentHtml.append("    };\n");
		fragmentHtml.append("	});\n");
		fragmentHtml.append("\n");
		fragmentHtml.append("  var markerBounds = new google.maps.LatLngBounds();\n");
		fragmentHtml.append("  map.data.addListener('addfeature', function(event) {\n");
		fragmentHtml.append("  	event.feature.setProperty(\"isMarkerFromDB\", true);\n");
		fragmentHtml.append("   markerBounds.extend(event.feature.getGeometry().get());\n");
		fragmentHtml.append("  });\n");
		fragmentHtml.append("\n");
		fragmentHtml.append("\n");
		fragmentHtml.append("	var infowindow = new google.maps.InfoWindow();\n");
		fragmentHtml.append("\n");
		fragmentHtml.append("  map.data.addListener('mouseover', function(event) {\n");
		fragmentHtml.append("  	if (event.feature.getProperty(\"isMarkerFromDB\")){\n");
		fragmentHtml.append("    	var geometry = event.feature.getGeometry ();\n");
		fragmentHtml.append("      var latLngMarker = geometry.get();\n");
		fragmentHtml.append("      \n");
		fragmentHtml.append("      infowindow.setPosition(latLngMarker);\n");
		fragmentHtml.append("      \n");
		fragmentHtml.append("      infowindow.setOptions({pixelOffset: new google.maps.Size(0,-30)});\n");
		fragmentHtml.append("      \n");
		fragmentHtml.append("      infowindow.setContent(\n");
		fragmentHtml.append("      	\"<p><b>\" + event.feature.getProperty(\"description\") + \"</b></p><div>\" + \n");
		fragmentHtml.append("      	event.feature.getProperty(\"title\") + \"</div>\");\n");
		fragmentHtml.append("    	\n");
		fragmentHtml.append("      infowindow.open(map);\n");
		fragmentHtml.append("    }\n");
		fragmentHtml.append("  });\n");
		fragmentHtml.append("  \n");
		fragmentHtml.append("	map.data.addGeoJson (markerData);\n");
		fragmentHtml.append("   google.maps.event.addListenerOnce(map, 'idle', function() { ");
		fragmentHtml.append("     map.fitBounds(markerBounds);\n");
		fragmentHtml.append("   })");
		fragmentHtml.append("}");
		fragmentHtml.append("\n");
		fragmentHtml.append("	</script>\n");
		fragmentHtml.append("\n");
		fragmentHtml.append("</head>\n");
		fragmentHtml.append("<body>\n");
		fragmentHtml.append("	<div id=\"map\"></div>\n");
		fragmentHtml.append("	<script defer src=\"https://maps.googleapis.com/maps/api/js?key=");
		fragmentHtml.append(API_KEY);
		fragmentHtml.append("&callback=initMap\"></script>");
		fragmentHtml.append("\n");
		fragmentHtml.append("</body>\n</html>\n");
		
		return fragmentHtml.toString();
	}

	@Override
	public void setLatLong(List<MLocation> locations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLatLong(MLocation location) {
		// TODO Auto-generated method stub
		
	}

}
