// @flow

import React, { Component } from 'react'
import { Map, TileLayer, Marker, Popup } from 'react-leaflet'
import L from 'leaflet'

/*
export const pointerIcon = new L.Icon({
  iconUrl: '../assets/pointerIcon.svg',
  iconRetinaUrl: '../assets/pointerIcon.svg',
  iconAnchor: [5, 55],
  popupAnchor: [10, -44],
  iconSize: [25, 55],
  shadowUrl: '../assets/marker-shadow.png',
  shadowSize: [68, 95],
  shadowAnchor: [20, 92],
})

export const suitcasePoint = new L.Icon({
  iconUrl: '../assets/suitcaseIcon.svg',
  iconRetinaUrl: '../assets/suitcaseIcon.svg',
  iconAnchor: [20, 40],
  popupAnchor: [0, -35],
  iconSize: [40, 40],
  shadowUrl: '../assets/marker-shadow.png',
  shadowSize: [29, 40],
  shadowAnchor: [7, 40],
})
*/

const weekend = L.divIcon({
  className: 'custom-div-icon',
  html: "<div style='background-color:#c30b82;' class='marker-pin'></div><i class='material-icons'>weekend</i>",
  iconSize: [30, 42],
  iconAnchor: [15, 42]
});

const icon = L.divIcon({
  className: 'custom-div-icon',
  html: "<div style='background-color:#4838cc;' class='marker-pin'></div><i class='material-icons'>accessible</i>",
  iconSize: [30, 42],
  iconAnchor: [15, 42]
});

const numberIcon = L.divIcon({
  className: 'custom-div-icon',
  html: "<div style='background-color:#4838cc;' class='marker-pin'><i class='material-icons'>12</i></div>",
  iconSize: [30, 42],
  iconAnchor: [15, 42]
});



export default class CustomIcons extends Component{
  state = {
    lat: 51.505,
    lng: -0.09,
    zoom: 13,
  }

  render() {
    const position = [this.state.lat, this.state.lng]
    const position2 = [51.50503625326346, -0.10088324546813966]
    return (
      <Map center={position} zoom={this.state.zoom}>
        <TileLayer
          attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={position} icon={weekend}>
          <Popup>
            A pretty CSS3 popup. <br /> Easily customizable.
          </Popup>
        </Marker>
        <Marker position={position2} icon={icon}>
          <Popup>
            A pretty CSS3 popup. <br /> Easily customizable.
          </Popup>
        </Marker>
        <Marker position={[51.50503625326346, -0.147]} icon={numberIcon}>
          <Popup>
            A pretty CSS3 popup. <br /> Easily customizable.
          </Popup>
        </Marker>
      </Map>
    )
  }
}
