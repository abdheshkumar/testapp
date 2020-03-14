// @flow

import Leaflet from 'leaflet';
import React, { StrictMode } from 'react';

import SimpleExample from './examples/SimpleExample';
import EventsExample from './examples/EventsExample';
import ViewportExample from './examples/ViewportExample';
import VectorLayersExample from './examples/VectorLayersExample';
import OtherLayersExample from './examples/OtherLayersExample';
import GeoJSONExample from './examples/GeoJSONExample';
import ZoomControlExample from './examples/ZoomControlExample';
import LayersControlExample from './examples/LayersControlExample';
import DraggableExample from './examples/DraggableExample';
import BoundsExample from './examples/BoundsExample';
import CustomComponentExample from './examples/CustomComponent';
import AnimateExample from './examples/AnimateExample';
import TooltipExample from './examples/TooltipExample';
import PaneExample from './examples/PaneExample';
import WMSTileLayerExample from './examples/WMSTileLayerExample';
import VideoOverlayExample from './examples/VideoOverlayExample';
import CustomIcons from './examples/CustomIcons';
import SVGOverlayExample from './examples/SVGOverlayExample';
import MostLocationInTheWorld from './examples/MostLocationInTheWorld';
import { MostPopularMeetupInTheWorld } from './meetup/MostPopularMeetupInTheWorld';
import MapExample from './meetup/MapExample';
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link,
  useParams,
} from 'react-router-dom';
import { TrendingTopic } from './meetup/TrendingTopic';
import Home from './meetup/Home';
Leaflet.Icon.Default.imagePath =
  '//cdnjs.cloudflare.com/ajax/libs/leaflet/1.3.4/images/';

import { makeStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing(2),
    textAlign: 'center',
    color: theme.palette.text.secondary,
  },
}));

const App = () => {
  return (
    <StrictMode>
      <Router>
        <div>
          <ul>
            <li>
              <Link to="/">Home</Link>
            </li>
            <li>
              <Link to="/most-popular-events">
                Most popular events in the world
              </Link>
            </li>
          </ul>

          <hr />

          <Switch>
            <Route exact path="/">
              <Home />
            </Route>
            <Route path="/most-popular-events">
              <MostPopularMeetupInTheWorld />
            </Route>
            <Route
              path="/trending-topics-by-country/:country"
              children={<TrendingTopic />}
            />
          </Switch>
        </div>
      </Router>
      {/*<h1>React-Leaflet examples</h1>
    <MostLocationInTheWorld />
    <h2>Popup with Marker</h2>
    <SimpleExample />
    <h2>Events</h2>
    <p>Click the map to show a marker at your detected location</p>
    <EventsExample />
    <h2>Viewport</h2>
    <p>Click the map to reset it to its original position</p>
    <ViewportExample />
    <h2>Vector layers</h2>
    <VectorLayersExample />
    <h2>SVG Overlay</h2>
    <SVGOverlayExample />
    <h2>Other layers</h2>
    <OtherLayersExample />
    <h2>GeoJSON with Popup</h2>
    <GeoJSONExample />
    <h2>Tooltips</h2>
    <TooltipExample />
    <h2>Zoom control</h2>
    <ZoomControlExample />
    <h2>Layers control</h2>
    <LayersControlExample />
    <h2>Panes</h2>
    <PaneExample />
    <h2>Draggable Marker</h2>
    <DraggableExample />
    <h2>Map view by bounds</h2>
    <p>Click a rectangle to fit the map to its bounds</p>
    <BoundsExample />
    <h2>List of markers (custom component)</h2>
    <CustomComponentExample />
    <h2>Animate</h2>
    <p>Click the map to move to the location</p>
    <AnimateExample />
    <h2>WMS tile layer</h2>
    <WMSTileLayerExample />
    <h2>Video overlay</h2>
    <VideoOverlayExample />
    <h2>Custom Icons</h2>*/}
    </StrictMode>
  );
};
export default App;
