import React, { Fragment } from 'react';
import axios from 'axios';
import { Map, TileLayer } from 'react-leaflet';
import { getApiPath, Context } from './utils';
import MarkersList from './MarkersList';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { TrendingTopic } from './TrendingTopic';
import { makeStyles } from '@material-ui/core/styles';

const reducer = (state, action) => {
  switch (action.type) {
    case 'eventsLoaded':
      return { ...state, events: action.payload };
    case 'trendingTopicsLoaded':
      return { ...state, trendingTopics: action.payload };
    case 'countrySelected':
      return { ...state, country: action.payload };
    default:
      return state;
  }
};

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing(2),
    textAlign: 'center',
    color: theme.palette.text.secondary,
    paddingBottom: 50,
  },
}));

const initialState = { events: [], country: undefined, trendingTopics:[] };

export const MostPopularMeetupInTheWorld = () => {
  const classes = useStyles();
  const [state, dispatch] = React.useReducer(reducer, initialState);

  React.useEffect(() => {
    const fetchData = async () => {
      const response = await axios.get(getApiPath('events'));
      dispatch({ type: 'eventsLoaded', payload: response.data });
    };
    fetchData().catch(error => {
      console.log(error);
    });
  }, []);

  return (
    <Context.Provider value={dispatch}>
      <Grid container justify="center" spacing={2}>
        <Grid sm={8} item>
          <Paper className={classes.paper}>
            <Map center={[51.505, -0.09]} zoom={0.7}>
              <MarkersList markers={state.events} />
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            </Map>
          </Paper>
        </Grid>
        <Grid sm={4} item>
          <Paper
            className={classes.paper}
            style={{ overflowY: 'scroll', height: '600px' }}>
            <TrendingTopic country={state.country} />
          </Paper>
        </Grid>
      </Grid>
    </Context.Provider>
  );
};
