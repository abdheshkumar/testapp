const object = {
  a: 1,
  b: 2,
  c: 3,
};
const array = Object.entries(object);
console.log(array); // [['a', 1], ['b', 2], ['c', 3]]
const newArray = array.map(([key, value]) => [key, value * 2]);
console.log(newArray); // [['a', 2], ['b', 4], ['c', 6]]

const newObject = newArray.reduce((accumulator, [key, value]) => {
  accumulator[key] = value;
  return accumulator;
}, {});
console.log(newObject);
