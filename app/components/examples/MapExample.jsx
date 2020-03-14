import React from 'react';
import { render } from 'react-dom';
import { Map, Marker, Popup, TileLayer,LayersControl,FeatureGroup } from 'react-leaflet';
import HeatmapLayer from 'react-leaflet-heatmap-layer';
import { addressPoints } from './data/realworld.10000.js';
 

export default class MapExample extends React.Component {
  state = {
    lat: 51.505,
    lng: -0.09,
    zoom: 13,
  }

  render() {
    const position = [this.state.lat, this.state.lng]
      return (
        <div>
        <Map center={[0,0]} zoom={13} style={{ height: '500px' }} >
              <LayersControl>
                <LayersControl.BaseLayer name="Base" checked>
                  <TileLayer
                    url="http://{s}.tile.osm.org/{z}/{x}/{y}.png"
                    attribution="&copy; <a href=http://osm.org/copyright>OpenStreetMap</a> contributors"
                  />
                </LayersControl.BaseLayer>
                <LayersControl.Overlay name="Heatmap" checked>
                  <FeatureGroup color="purple">
                    <Marker position={[50.05, -0.09]} >
                      <Popup>
                        <span>A pretty CSS3 popup.<br /> Easily customizable. </span>
                      </Popup>
                    </Marker>
                    <HeatmapLayer
                      fitBoundsOnLoad
                      fitBoundsOnUpdate
                      points={addressPoints}
                      longitudeExtractor={m => m[1]}
                      latitudeExtractor={m => m[0]}
                      intensityExtractor={m => parseFloat(m[2])}
                    />
                  </FeatureGroup>
                </LayersControl.Overlay>
                <LayersControl.Overlay name="Marker">
                  <FeatureGroup color="purple">
                    <Marker position={position} >
                      <Popup>
                        <span>A pretty CSS3 popup.<br /> Easily customizable. </span>
                      </Popup>
                    </Marker>
                  </FeatureGroup>
                </LayersControl.Overlay>
              </LayersControl>
            </Map>
        </div>
      );
    }

}
