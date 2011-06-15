package edu.nrao.dss.client;

import com.google.gwt.junit.client.GWTTestCase;

import edu.nrao.dss.client.util.Conversions;

public class TestConversions extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "edu.nrao.dss.Nubbles";
	}
	
	public void testRadiansToTime() {
		assertEquals("03:49:11.0", Conversions.radiansToTime(1.0));
		assertEquals("-03:49:11.0", Conversions.radiansToTime(-1.0));
		assertEquals("00:22:55.1", Conversions.radiansToTime(0.1));
		assertEquals("-00:22:55.1", Conversions.radiansToTime(-0.1));
		assertEquals("05:59:49.0", Conversions.radiansToTime(1.57));
		assertEquals("-05:59:49.0", Conversions.radiansToTime(-1.57));
		assertEquals("00:00:00.0", Conversions.radiansToTime(0.0));
	}
	
	public void testRadiansToSexagesimal() {
		assertEquals("57:17:44.8", Conversions.radiansToSexagesimal(1.0));
		assertEquals("-57:17:44.8", Conversions.radiansToSexagesimal(-1.0));
		assertEquals("00:34:22.6", Conversions.radiansToSexagesimal(0.01));
		assertEquals("-00:34:22.6", Conversions.radiansToSexagesimal(-0.01));
		assertEquals("89:57:15.7", Conversions.radiansToSexagesimal(1.57));
		assertEquals("-89:57:15.7", Conversions.radiansToSexagesimal(-1.57));
		assertEquals("00:00:00.0", Conversions.radiansToSexagesimal(0.0));
	}
	
	public void testDegreesToTime() {
		assertEquals("01:00:00.0", Conversions.degreesToTime(15.0));
		assertEquals("-01:00:00.0", Conversions.degreesToTime(-15.0));
		assertEquals("00:30:00.0", Conversions.degreesToTime(7.5));
		assertEquals("-00:30:00.0", Conversions.degreesToTime(-7.5));
		assertEquals("06:00:00.0", Conversions.degreesToTime(90.0));
		assertEquals("-06:00:00.0", Conversions.degreesToTime(-90.0));
		assertEquals("00:00:00.0", Conversions.degreesToTime(0.0));
	}
	
	
	public void testDegreesToSexagesimal() {
		assertEquals("15:00:00.0", Conversions.degreesToSexagesimal(15.0));
		assertEquals("-15:00:00.0", Conversions.degreesToSexagesimal(-15.0));
		assertEquals("00:30:00.0", Conversions.degreesToSexagesimal(0.5));
		assertEquals("-00:30:00.0", Conversions.degreesToSexagesimal(-0.5));
		assertEquals("90:00:00.0", Conversions.degreesToSexagesimal(90.0));
		assertEquals("-90:00:00.0", Conversions.degreesToSexagesimal(-90.0));
		assertEquals("00:00:00.0", Conversions.degreesToSexagesimal(0.0));
	}
	
	public void testTimeToRadians() {
		assertEquals(Math.PI, Conversions.timeToRadians("12:00:00.0"));
		assertEquals(-Math.PI, Conversions.timeToRadians("-12:00:00.0"));
		assertEquals(0.0, Conversions.timeToRadians("00:00:00.0"));
	}
	
	public void testSexagesimalToRadians() {
		assertEquals(Math.PI/4.0, Conversions.sexagesimalToRadians("45:00:00.0"));
		assertEquals(-Math.PI/4.0, Conversions.sexagesimalToRadians("-45:00:00.0"));
		assertEquals(0.0, Conversions.sexagesimalToRadians("00:00:00.0"));
	}
	
	public void testTimeToDegrees() {
		assertEquals(180.0, Conversions.timeToDegrees("12:00:00.0"));
		assertEquals(-180.0, Conversions.timeToDegrees("-12:00:00.0"));
		assertEquals(0.0, Conversions.timeToDegrees("00:00:00.0"));
	}
	
	public void testSexigesimalToDegrees() {
		assertEquals(45.0, Conversions.sexigesimalToDegrees("45:00:00.0"));
		assertEquals(-90.0, Conversions.sexigesimalToDegrees("-90:00:00.0"));
		assertEquals(0.0, Conversions.sexigesimalToDegrees("00:00:00.0"));
	}
	
	public void testRadiansDegrees() {
		assertEquals(Math.PI, Conversions.degreesToRadians(180.0));
		assertEquals(90.0, Conversions.radiansToDegrees(Math.PI/2.0));
		assertEquals(-Math.PI, Conversions.degreesToRadians(-180.0));
		assertEquals(-90.0, Conversions.radiansToDegrees(-Math.PI/2.0));
		
		
	}
}
