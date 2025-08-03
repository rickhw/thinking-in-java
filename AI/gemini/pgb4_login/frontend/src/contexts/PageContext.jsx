import React, { createContext, useContext, useState } from 'react';

const PageContext = createContext();

export const usePageTitle = () => {
    const context = useContext(PageContext);
    if (!context) {
        throw new Error('usePageTitle must be used within a PageProvider');
    }
    return context;
};

export const PageProvider = ({ children }) => {
    const [pageTitle, setPageTitle] = useState('Message Board');

    const value = {
        pageTitle,
        setPageTitle
    };

    return (
        <PageContext.Provider value={value}>
            {children}
        </PageContext.Provider>
    );
};