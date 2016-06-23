package unife.icedroid.core.routingalgorithms;

import unife.icedroid.utils.Settings;

public class RoutingAlgorithmFactory {
	public static RoutingAlgorithm makeRoutingAlgorithm (Settings.RoutingAlgorithm routingAlgorithmType) {
		switch (routingAlgorithmType) {
		case SPRAY_AND_WAIT:
			return new SprayAndWaitThread();
		}
		
		return null;
	}
}
