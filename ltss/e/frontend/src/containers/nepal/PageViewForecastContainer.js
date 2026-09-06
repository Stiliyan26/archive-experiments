import React, { useState } from 'react';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Form from 'react-bootstrap/Form';
import Image from 'react-bootstrap/Image';
import { withTranslation } from 'react-i18next';


const initialValue = {
    city: "",
    maxTemp: "",
    minTemp: "",
    currentTemp: "",
    weather: "",
    sunrise: "",
    sunset: "",
    iconId: ""
}

const PageViewForecastContainer = () => {

    const [result, setResult] = useState(initialValue)

    const dropdownValues = [
        {
            cityName: "Априлци",
            lat: "42.83193689190003",
            lon: "24.926008495499854"
        },
        {
            cityName: "София",
            lat: "42.698334",
            lon: "23.319941"
        },
        {
            cityName: "Варна",
            lat: "43.204666",
            lon: "27.910543"
        },
        {
            cityName: "Бургас",
            lat: "42.510578",
            lon: "27.461014"
        },
        {
            cityName: "Пловдив",
            lat: "42.136097",
            lon: "24.742168"
        }
    ]

    const getForecats = (e) => {

        fetch(`https://api.openweathermap.org/data/2.5/weather?lat=${e.lat}&lon=${e.lon}&units=metric&appid=c61127a642ada918db7bfda684f665d9`)
            .then(response => response.json())
            .then(
                //data => console.log(data),
                data => setResult((oldResult) => ({
                    ...oldResult,
                    city: e.cityName,
                    maxTemp: data.main.temp_max,
                    minTemp: data.main.temp_min,
                    currentTemp: data.main.temp,
                    weather: data.weather[0].main,
                    sunrise: new Date(data.sys.sunrise * 1000).toLocaleString('default', 'EET'),
                    sunset: new Date(data.sys.sunset * 1000).toLocaleString('default', 'EET'),
                    iconId: data.weather[0].icon
                }))
            )
            .catch(error => console.error(error));
    }

    return (
        <Form style={{ margin: "15px" }}>
            <Form.Row>
                <Form.Group style={{ margin: "10px" }}>
                    <DropdownButton id="dropdown-basic-button" title="Избери град">
                        {dropdownValues.map((city) =>
                            <Dropdown.Item key={city.lat + city.lon} onSelect={() => getForecats(city)}>
                                {city.cityName}
                            </Dropdown.Item>
                        )}
                    </DropdownButton>
                    {result.iconId == "" ? "" : <Image style={{ margin: "10px" }} src={`https://openweathermap.org/img/wn/${result.iconId}@2x.png`} thumbnail />}
                </Form.Group>
                <Form.Group style={{ margin: "10px" }}>
                    <Form.Row>Град: {result.city}</Form.Row>
                    <Form.Row>Време: {result.weather}</Form.Row>
                    <Form.Row>Температура: {result.currentTemp} &deg;C</Form.Row>
                    <Form.Row>Изгрев: {result.sunrise}</Form.Row>
                    <Form.Row>Залез: {result.sunset}</Form.Row>
                    <Form.Row>Макс. Температура: {result.maxTemp} &deg;C</Form.Row>
                    <Form.Row>Mин. Температура: {result.minTemp} &deg;C</Form.Row>
                </Form.Group>
            </Form.Row>
        </Form>
    )
}

export default PageViewForecastContainer;