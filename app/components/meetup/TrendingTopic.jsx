import React from 'react';
import { getApiPath, useStyles } from './utils';
import Axios from 'axios';
import ListComponent from '../ListComponent';
import Typography from '@material-ui/core/Typography';

export const TrendingTopic = ({ country }) => {
  const classes = useStyles();
  const [data, setData] = React.useState([]);

  React.useEffect(() => {
    const fetchTrendingTopics = async () => {
      const response = await Axios.get(getApiPath(`trending/${country}`));
      console.log(response.data);
      setData(response.data);
    };
    country && fetchTrendingTopics();
  }, [country]);

  return (
    <>
      <Typography className={classes.text} variant="h5" gutterBottom>
        Showing trending topics of the clicked country: {country}
      </Typography>
      <ListComponent
        data={data}
        fromMessageToListData={d => ({ id: d.topics, primary: d.topics })}
      />
    </>
  );
};
