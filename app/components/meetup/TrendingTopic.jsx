import React from 'react';
import { getApiPath } from './utils';
import Axios from 'axios';
import { useParams } from 'react-router-dom';

export const TrendingTopic = () => {
  const [topics, setTopics] = React.useState([]);
  let { country } = useParams();

  React.useEffect(() => {
    const fetchTrendingTopics = async () => {
      const response = await Axios.get(getApiPath(`trending/${country}`));
      setTopics(response.data);
    };
    country && fetchTrendingTopics();
  }, []);
  return (
    <div>
      <h4>Trending topics by country: {country}</h4>
      {topics.map(t => (
        <div key={t.topics}>{t.topics}</div>
      ))}
    </div>
  );
};
