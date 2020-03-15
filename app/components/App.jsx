// @flow

import Leaflet from 'leaflet';
import React from 'react';
import { MostPopularMeetupInTheWorld } from './meetup/MostPopularMeetupInTheWorld';

import { TrendingTopic } from './meetup/TrendingTopic';
import { Context } from './meetup/utils';
import CssBaseline from '@material-ui/core/CssBaseline';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import RsvpLive from './meetup/RsvpLive';
import Box from '@material-ui/core/Box';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import { RsvpMap } from './meetup/RsvpMap';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
Leaflet.Icon.Default.imagePath =
  '//cdnjs.cloudflare.com/ajax/libs/leaflet/1.3.4/images/';

const useStyles = makeStyles(theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  },
  appBar: {
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
  },
  paper: {
    padding: theme.spacing(2),
    textAlign: 'center',
    color: theme.palette.text.secondary,
  },
}));

const a11yProps = index => {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
};
const TabPanel = props => {
  const { children, value, index, ...other } = props;

  return (
    <Typography
      component="div"
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}>
      {value === index && <Box p={3}>{children}</Box>}
    </Typography>
  );
};

const App = () => {
  const classes = useStyles();

  const [value, setValue] = React.useState(0);
  return (
    <React.Fragment>
      <CssBaseline />
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" className={classes.title}>
            ING Meetup app
          </Typography>
        </Toolbar>
      </AppBar>
      <Grid container direction="row" justify="center" className={classes.root}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Paper className={classes.paper}>
              <Box textAlign="center" m={3}></Box>
            </Paper>
          </Grid>
          <Grid item xs={12}>
            <Paper className={classes.paper}>
              <AppBar position="static">
                <Tabs
                  value={value}
                  onChange={(event, newValue) => setValue(newValue)}
                  aria-label="Menu">
                  <Tab label="Live stream of meetup" {...a11yProps(0)} />
                  <Tab label="Most popular events" {...a11yProps(1)} />
                </Tabs>
              </AppBar>
            </Paper>
          </Grid>
          <Grid item xs={12}>
            <TabPanel value={value} index={0}>
              <RsvpLive />
            </TabPanel>
            <TabPanel value={value} index={1}>
              <MostPopularMeetupInTheWorld />
            </TabPanel>
            {/* <RsvpLive /> */}
          </Grid>
        </Grid>
      </Grid>
    </React.Fragment>
  );
};
export default App;
