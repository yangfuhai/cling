/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.cling.osgi.basedriver.present;

/*
 * UPnPEventHandler captures OSGi UPnP events. When handling a
 * event it compares all the registered UPnPEvent listeners
 * against the source of the event. If a listener matches the
 * source it will notify that listener.
 */

import java.util.Dictionary;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPService;
import org.osgi.util.tracker.ServiceTracker;

class UPnPEventHandler implements EventHandler {
    private static final Logger logger = Logger.getLogger(UPnPEventHandler.class.getName());
	private ServiceTracker tracker;
	
	public UPnPEventHandler(BundleContext context) {
		String string = String.format("(%s=%s)", 
			Constants.OBJECTCLASS , UPnPEventListener.class.getName()
			);
		try {
			Filter filter = context.createFilter(string);
			
			tracker = new ServiceTracker(context, filter, null);
			tracker.open();
		} catch (InvalidSyntaxException e) {
			logger.severe("Cannot create UPnPEventListener tracker.");
			logger.severe(e.getMessage());
		}
	}

	@Override
	public void handleEvent(Event event) {
		logger.entering(this.getClass().getName(), "handleEvent", new Object[] { event });
		
		ServiceReference[] references = tracker.getServiceReferences();
		if (references != null) {
			for (ServiceReference reference : references) {
				boolean matches = true;
				Filter filter = (Filter) reference.getProperty(UPnPEventListener.UPNP_FILTER);
				if (filter != null) {
					matches = event.matches(filter);
				}
				
				if (matches) {
					UPnPEventListener listener = (UPnPEventListener) tracker.getService(reference);
					listener.notifyUPnPEvent(
						(String) event.getProperty(UPnPDevice.UDN),
						(String) event.getProperty(UPnPService.ID),
						(Dictionary) event.getProperty("upnp.events")
						);
				}
			}
		}
	}
}

