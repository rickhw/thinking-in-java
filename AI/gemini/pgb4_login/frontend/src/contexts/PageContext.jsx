import React, { createContext, useContext, useState, useEffect } from 'react';

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
    const [pageDescription, setPageDescription] = useState('');
    const [pageAuthor, setPageAuthor] = useState('');
    const [pagePublishedTime, setPagePublishedTime] = useState('');

    // Update document title when pageTitle changes
    useEffect(() => {
        document.title = pageTitle;
    }, [pageTitle]);

    // Update meta description when pageDescription changes
    useEffect(() => {
        if (pageDescription) {
            let metaDescription = document.querySelector('meta[name="description"]');
            if (!metaDescription) {
                metaDescription = document.createElement('meta');
                metaDescription.name = 'description';
                document.head.appendChild(metaDescription);
            }
            metaDescription.content = pageDescription;
        }
    }, [pageDescription]);

    // Update Open Graph meta tags
    useEffect(() => {
        if (pageTitle || pageDescription) {
            // Update or create og:title
            let ogTitle = document.querySelector('meta[property="og:title"]');
            if (!ogTitle) {
                ogTitle = document.createElement('meta');
                ogTitle.setAttribute('property', 'og:title');
                document.head.appendChild(ogTitle);
            }
            ogTitle.content = pageTitle;

            // Update or create og:description
            if (pageDescription) {
                let ogDescription = document.querySelector('meta[property="og:description"]');
                if (!ogDescription) {
                    ogDescription = document.createElement('meta');
                    ogDescription.setAttribute('property', 'og:description');
                    document.head.appendChild(ogDescription);
                }
                ogDescription.content = pageDescription;
            }

            // Update or create og:type
            let ogType = document.querySelector('meta[property="og:type"]');
            if (!ogType) {
                ogType = document.createElement('meta');
                ogType.setAttribute('property', 'og:type');
                document.head.appendChild(ogType);
            }
            ogType.content = pageDescription ? 'article' : 'website';

            // Update or create og:url
            let ogUrl = document.querySelector('meta[property="og:url"]');
            if (!ogUrl) {
                ogUrl = document.createElement('meta');
                ogUrl.setAttribute('property', 'og:url');
                document.head.appendChild(ogUrl);
            }
            ogUrl.content = window.location.href;
        }
    }, [pageTitle, pageDescription]);

    // Update structured data (JSON-LD)
    useEffect(() => {
        // Remove existing structured data first
        const existingScript = document.querySelector('script[type="application/ld+json"]');
        if (existingScript) {
            existingScript.remove();
        }

        // Only add structured data if we have message content
        if (pageDescription && pageAuthor && pagePublishedTime) {
            const structuredData = {
                "@context": "https://schema.org",
                "@type": "SocialMediaPosting",
                "headline": pageTitle,
                "author": {
                    "@type": "Person",
                    "name": pageAuthor
                },
                "datePublished": pagePublishedTime,
                "text": pageDescription,
                "url": window.location.href
            };

            const script = document.createElement('script');
            script.type = 'application/ld+json';
            script.textContent = JSON.stringify(structuredData);
            document.head.appendChild(script);
        }
    }, [pageTitle, pageDescription, pageAuthor, pagePublishedTime]);

    const setPageMeta = ({ title, description, author, publishedTime }) => {
        if (title) setPageTitle(title);
        if (description) setPageDescription(description);
        if (author) setPageAuthor(author);
        if (publishedTime) setPagePublishedTime(publishedTime);
    };

    const resetPageMeta = () => {
        setPageTitle('Message Board');
        setPageDescription('');
        setPageAuthor('');
        setPagePublishedTime('');
        
        // Remove structured data
        const existingScript = document.querySelector('script[type="application/ld+json"]');
        if (existingScript) {
            existingScript.remove();
        }
    };

    const value = {
        pageTitle,
        pageDescription,
        pageAuthor,
        pagePublishedTime,
        setPageTitle,
        setPageDescription,
        setPageAuthor,
        setPagePublishedTime,
        setPageMeta,
        resetPageMeta
    };

    return (
        <PageContext.Provider value={value}>
            {children}
        </PageContext.Provider>
    );
};