import React, { forwardRef } from "react";
import Spinner from "react-bootstrap/Spinner";

const LoadingView = forwardRef<
    HTMLDivElement,
    React.ComponentPropsWithoutRef<"div">
>((props, ref) => (
    <div
        ref={ref}
        className="d-flex flex-row justify-content-center align-items-center"
        {...props}
    >
        <Spinner animation="border" variant="primary" />
        <div className="ps-2">Loading...</div>
    </div>
));

export default LoadingView;
