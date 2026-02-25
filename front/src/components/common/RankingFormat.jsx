import PropTypes from "prop-types";
import {Badge} from "react-bootstrap";
import React from "react";

const RankingFormat = ({rank}) => {
    return(
        <span>
            {!rank && '❌'}
            {rank === 1 && '🥇'}
            {rank === 2 && '🥈'}
            {rank === 3 && '🥉'}
            {rank > 3 && <Badge bg={"warning"}>{rank}</Badge>}
        </span>
    )
}

RankingFormat.propTypes = {
    rank: PropTypes.number.isRequired
};

export default RankingFormat;